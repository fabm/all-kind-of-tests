package pt.fabm;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.RockerOutputFactory;
import com.fizzed.rocker.runtime.AbstractRockerOutput;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Test;
import views.TableLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestJavaParser {

    @Test
    public void testAddFragment() throws URISyntaxException, IOException {
        final URI uri = getClass().getResource("/MySampleClass.txt").toURI();
        String content = new String(Files.readAllBytes(Paths.get(uri)));
        CompilationUnit cu = JavaParser.parse(content);

        ClassOrInterfaceDeclaration theClass = cu.findAll(ClassOrInterfaceDeclaration.class).get(0);

        List<MethodDeclaration> met = theClass.getMethodsByName("getThePrivateVarChanged");
        MethodDeclaration theClone = met.get(0).clone();
        theClone.setName("theClonedMethod");
        theClass.addMember(theClone);

        int lineToSplit = met.get(0).getEnd().orElseThrow(() -> new IllegalStateException("no method")).line;

        LinesReader lr = () -> Files.lines(Paths.get(uri));

        final URI uriMethodToAdd = getClass().getResource("/methodToAdd.txt").toURI();
        String result = Files.readAllLines(Paths.get(uriMethodToAdd))
                .stream()
                .map("    "::concat)
                .collect(Collectors.joining("\n"));

        final URI uriResult = getClass().getResource("/result.txt").toURI();
        Assert.assertEquals(
                new String(Files.readAllBytes(Paths.get(uriResult))),
                LinesAdder.addFragments(lr, i -> i == lineToSplit, i -> result)
        );

    }

    @Test
    public void testAddToConstuctor() throws URISyntaxException, IOException {
        final URI uri = getClass().getResource("/MySampleClass.txt").toURI();
        String content = new String(Files.readAllBytes(Paths.get(uri)));
        CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(content));

        ClassOrInterfaceDeclaration theClass = (cu.findAll(ClassOrInterfaceDeclaration.class).get(0));


        ConstructorDeclaration constructor = theClass.getConstructors().get(0);
        for (int i = 1; i < 6; i++) {
            constructor.getBody().addAndGetStatement("thePrivateVar" + i + " = \"thePrivate" + i + "\"");
        }

        final URI uriResult = getClass().getResource("/resultConstructor.txt").toURI();
        Assert.assertEquals(
                new String(Files.readAllBytes(Paths.get(uriResult))),
                LexicalPreservingPrinter.print(theClass)
        );
    }

    @Test
    public void testsSelectElements() throws URISyntaxException, IOException {
        final URI uri = getClass().getResource("/result.txt").toURI();
        String content = new String(Files.readAllBytes(Paths.get(uri)));
        CompilationUnit cu = JavaParser.parse(content);

        ClassOrInterfaceDeclaration theClass = (cu.findAll(ClassOrInterfaceDeclaration.class).get(0));

        List<MethodDeclaration> methods = theClass.getMethods();
        Map<String, Integer> mapInit = new HashMap<>();
        Map<String, Integer> mapEnd = new HashMap<>();
        Assert.assertEquals(3, methods.size());

        int ref = 31;

        mapInit.put("getThePrivateVarChanged", ref);
        mapInit.put("getThePrivateVar", ref + 13);
        mapInit.put("transform", ref + 33);

        mapEnd.put("getThePrivateVarChanged", ref + 12);
        mapEnd.put("getThePrivateVar", ref + 25);
        mapEnd.put("transform", ref + 35);

        for (MethodDeclaration currentMethod : methods) {
            int begin = currentMethod.getJavadocComment().flatMap(NodeWithRange::getBegin)
                    .orElseGet(() -> currentMethod.getBegin().get())
                    .line;
            int end = currentMethod.getEnd().get().line;
            Assert.assertEquals(mapInit.get(currentMethod.getName().asString()).intValue(), begin);
            Assert.assertEquals(mapEnd.get(currentMethod.getName().asString()).intValue(), end);
        }
    }

    @Test
    public void template() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        EmbeddedCassandraServerHelper.getCluster();
        Session session = EmbeddedCassandraServerHelper.getSession();
        CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        final ClassPathCQLDataSet dataSet = new ClassPathCQLDataSet("load.cql");
        cqlDataLoader.load(dataSet);
        Vertx vertx = Vertx.vertx();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        Single<ResultSet> rs = Single.create(source -> {
            Futures.addCallback(session.executeAsync("select id, value from myTable"),
                    new FutureCallback<ResultSet>() {
                        @Override
                        public void onSuccess(ResultSet result) {
                            source.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            source.tryOnError(t);
                        }
                    }, MoreExecutors.directExecutor()
            );
        });


        Observable<java.util.function.Function<String, String>> fn = rs.flatMapObservable(Observable::fromIterable)
                .map(row -> (java.util.function.Function<String, String>) row::getString);

        router.get("/hw").handler(rc -> {
            HttpServerResponse resp = rc.response();
            resp.setChunked(true);

            fn.toList()
                    .map(TableLine::template)
                    .flatMapObservable(this::getObservable)
                    .subscribe(resp::write, Throwable::printStackTrace, resp::end);
        });

        router.get("/stop").handler(rc -> {
            rc.response().end("stopping server...");
            server.close();
            vertx.setPeriodic(2000, event -> {
                countDownLatch.countDown();
            });
        });

        int httpPort = 8080;
        server.requestHandler(router::accept).listen(httpPort);

        WebClient client = WebClient.create(vertx);
        client.get(httpPort, "localhost", "/hw").rxSend()
                .subscribe(resp -> {
                    final Consumer<Buffer> bufferConsumer = fileContent -> {
                        Assert.assertEquals(fileContent.toString(), resp.bodyAsString());
                        countDownLatch.countDown();
                    };
                    vertx.fileSystem().rxReadFile(getClass().getResource("/expectedContent.txt").getFile())
                            .subscribe(bufferConsumer);
                }, throwable -> {
                    Assert.fail();
                    countDownLatch.countDown();
                });

        Assert.assertTrue(countDownLatch.await(10, TimeUnit.MINUTES));
    }

    private Observable<String> getObservable(RockerModel rockerModel) {

        class CustomRockerOutput extends AbstractRockerOutput<CustomRockerOutput> {

            private final ObservableEmitter<String> source;

            public CustomRockerOutput(ContentType contentType, String charsetName, int byteLength, ObservableEmitter<String> source) {
                super(contentType, charsetName, byteLength);
                this.source = source;
            }

            @Override
            public AbstractRockerOutput w(String string) throws IOException {
                source.onNext(string);
                return this;
            }

            @Override
            public AbstractRockerOutput w(byte[] bytes) throws IOException {
                source.onNext(new String(bytes, charset));
                return this;
            }

        }

        ObservableOnSubscribe<String> observableOS = new ObservableOnSubscribe<String>() {
            private boolean firstSubscriber = true;

            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                if (!firstSubscriber) {
                    emitter.tryOnError(new IllegalStateException("only one subscription is allowed"));
                    return;
                }
                firstSubscriber = false;
                RockerOutputFactory<? extends RockerOutput> factory = (contentType, charsetName) ->
                        new CustomRockerOutput(contentType, charsetName, 100, emitter);
                rockerModel.render(factory);
                emitter.onComplete();
            }
        };

        return Observable.create(observableOS);
    }



    @Test
    public void distFolder() throws InterruptedException {
        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        final StaticHandler requestHandler = StaticHandler.create().setWebRoot("dist");
        router.route("/*").handler(requestHandler).handler(rc -> {
            if (rc.normalisedPath().startsWith("/forms")) {
                rc.next();
                return;
            }
            System.out.println(rc.normalisedPath());
            rc.reroute("/");
        });

        server.requestHandler(router::accept).listen(8080);

        Thread.sleep(100000);

    }

    @Test
    public void checkHtml() {
        Vertx vertx = Vertx.vertx();
        WebClient client;
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("My-App/1.2.3");
        options.setKeepAlive(false);
        client = WebClient.create(vertx, options);
        String body = client.get(3000, "localhost", "/")
                .rxSend()
                .blockingGet().bodyAsString();

        System.out.println(body);
    }
}
