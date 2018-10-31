package pt.fabm.schedulers;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Assert;
import org.junit.Test;
import pt.fabm.dao.UserDao;
import pt.fabm.errors.handling.ValidationManager;
import pt.fabm.model.User;
import pt.fabm.processes.p0001.P0001;
import pt.fabm.processes.p0002.P0002;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestRx {

    @Test
    public void testVertxEndPoint() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        EmbeddedCassandraServerHelper.getCluster();
        Session session = EmbeddedCassandraServerHelper.getSession();
        CQLDataLoader cqlDataLoader = new CQLDataLoader(session);
        final ClassPathCQLDataSet dataSet = new ClassPathCQLDataSet("load.cql");
        cqlDataLoader.load(dataSet);

        ApplicationContext.getInstance().setSessionSupplier(() -> session);
        UserDao userDao = new UserDao();
        ApplicationContext.getInstance().setUserDaoSupplier(() -> userDao);
        ValidationManager validationManager = new ValidationManager();
        ApplicationContext.getInstance().setValidationManagerSupplier(() -> validationManager);
        ApplicationContext.getInstance().setExecutorSupplier(MoreExecutors::directExecutor);

        P0001 p0001 = new P0001();
        P0002 p0002 = new P0002();
        Vertx vertx = Vertx.vertx();

        Router router = Router.router(vertx);
        router.route("/user").method(HttpMethod.POST)
                .handler(HttpServerHelper.routeSubscription(p0001::execute, Buffer::buffer));
        router.route("/user/:id").method(HttpMethod.GET)
                .handler(HttpServerHelper.routeSubscription(p0002::execute, HttpServerHelper::jsonToBuffer));

        List<CountDownLatch> cdl = new ArrayList<>();
        cdl.add(new CountDownLatch(1));

        int httpPort = 8081;

        vertx.createHttpServer().requestHandler(router::accept)
                .rxListen(httpPort).subscribe(server -> {
        });

        WebClient client = WebClient.create(vertx);
        Buffer jsonObject = Buffer.newInstance(new JsonObject().put("id", "123").put("name", "nameTest").toBuffer());

        client.request(HttpMethod.POST, httpPort, "localhost", "/user").rxSendBuffer(jsonObject)
                .subscribe(resp -> {
                    Assert.assertEquals("ok", resp.bodyAsString());
                    cdl.get(0).countDown();
                }, throwable -> {
                    cdl.get(0).countDown();
                    cdl.get(0).countDown();
                    Assert.fail();
                });

        // wait to insert
        Assert.assertTrue(cdl.get(0).await(2, TimeUnit.MINUTES));
        cdl.set(0, new CountDownLatch(1));

        client.request(HttpMethod.GET, httpPort, "localhost", "/user/123").rxSend()
                .subscribe(resp -> {
                    JsonObject userJson = resp.bodyAsJsonObject();
                    Assert.assertEquals("123",userJson.getString("id"));
                    Assert.assertEquals("nameTest",userJson.getString("name"));
                    cdl.get(0).countDown();
                }, throwable -> {
                    cdl.get(0).countDown();
                    Assert.fail();
                });

        Assert.assertTrue(cdl.get(0).await(2, TimeUnit.MINUTES));



    }


}
