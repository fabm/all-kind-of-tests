package pt.fabm;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"pt.fabm.Entity"})
public class TestAnnotationProcess extends AbstractProcessor {
    private Logger logger;

    private Types typeUtils;
    private Elements elementUtils;
    private File buildDir;
    private Script script;
    private Messager messager;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        GroovyShell groovyShell = new GroovyShell();
        try {
            script = groovyShell.parse(new File(this.processingEnv.getOptions().get("script")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("process()");
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            logger.info("annotated element:{}, package:{}", annotatedElement, processingEnv.getElementUtils().getPackageOf(annotatedElement).getQualifiedName());

            try {
                generateEntityHelper(annotatedElement, processingEnv.getElementUtils().getPackageOf(annotatedElement).getQualifiedName().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    private String generateStatementToCreateKeyspace(Element element) throws IOException {
        Entity entity = element.getAnnotation(Entity.class);

        Binding binding = new Binding();
        binding.setVariable("log", LoggerFactory.getLogger("script"));
        script.setBinding(binding);
        script.run();
        Closure<String> getKeyspaceCl = Closure.class.cast(script.getProperty("getkeyspace"));
        Closure<String> getStrategyCl = Closure.class.cast(script.getProperty("getStrategy"));
        Closure<Integer> getReplicationFactorCl = Closure.class.cast(script.getProperty("getReplicationFactor"));
        final String keyspace = getKeyspaceCl.call(entity.table());
        final String strategy = getStrategyCl.call(entity.table());
        final Integer replicationFactor = getReplicationFactorCl.call(entity.table());


        StringBuilder builderKeyspace = new StringBuilder("CREATE KEYSPACE ")
                .append(keyspace).append(" WITH replication={'class':'")
                .append(strategy).append("', 'replication_factor':")
                .append(replicationFactor).append("}");

        logger.info("keyspace:{}", builderKeyspace.toString());
        return builderKeyspace.toString();
    }

    private Script createScript(String file) throws IOException {
        Binding binding = new Binding();
        binding.setVariable("log", LoggerFactory.getLogger("script"));
        script.setBinding(binding);
        script.run();
        return script;
    }

    private String generateCodeToTableCreation(Element element) throws IOException {
        ClassAnalyser ca = ClassAnalyser.from(element, typeUtils);
        Entity entityAnnotation = ca.getAnnotation(Entity.class);
        if (entityAnnotation.script().isEmpty()) return null;
        Script script = createScript(entityAnnotation.script());
        StringBuilder stringBuilder = new StringBuilder();

        Closure<String> toCassandraType = Closure.class.cast(script.getProperty("toCassandraType"));
        stringBuilder.append("create table ").append(entityAnnotation.table()).append("(\n");
        List<String> cKeys = new ArrayList<>();
        List<String> pKeys = new ArrayList<>();
        for (FieldAnalyser fieldAnalyser : ca.getFields()) {

            Field fieldAnnotation = fieldAnalyser.getAnnotation(Field.class);
            if (fieldAnnotation.keyType() == Field.KeyType.CLUSTERING_KEY) {
                cKeys.add(fieldAnnotation.value());
            } else if (fieldAnnotation.keyType() == Field.KeyType.PARTITION_KEY) {
                pKeys.add(fieldAnnotation.value());
            }
            String type = toCassandraType.call(entityAnnotation.table(), fieldAnalyser.getName(), fieldAnalyser.getType());
            stringBuilder.append(fieldAnnotation.value()).append(" ")
                    .append(type)
                    .append(",\n");
        }

        StringJoiner stringJoiner = new StringJoiner(",", "(",")");
        if (!pKeys.isEmpty()) {
            stringJoiner = stringJoiner.add(pKeys.stream().collect(Collectors.joining(", ", "(", ")")));
        }
        stringJoiner.add(cKeys.stream().collect(Collectors.joining(",")));

        stringBuilder.append("primary key ").append(stringJoiner);
        return stringBuilder.toString();
    }

    private void generateEntityHelper(Element element, String packageName) throws IOException {
        generateStatementToCreateKeyspace(element);

        //logger.info("codeCreation {}", generateCodeToTableCreation(element));
        StringJoiner namesJoiner = new StringJoiner(", ");
        StringJoiner questionMarksJoiner = new StringJoiner(",");
        StringJoiner keysJoiner = new StringJoiner(" AND ");
        final String tableName = element.getAnnotation(Entity.class).table();
        StringBuilder insertString = new StringBuilder("insert into ")
                .append(tableName)
                .append("(");
        StringBuilder selectString = new StringBuilder("select ");
        List<FieldSpec> consts = new ArrayList<>();

        CodeBlock cb;
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (!enclosedElement.getKind().isField()) continue;
            final Field tableField = enclosedElement.getAnnotation(Field.class);
            if (tableField == null) {
                logger.info("annotation Field is null in field:{}", enclosedElement);
            } else {
                if (tableField.keyType() == Field.KeyType.CLUSTERING_KEY) {
                    keysJoiner.add(tableField.value() + "=?");
                }
                logger.info("type name:{}", typeUtils.asElement(enclosedElement.asType()).toString());
                logger.info("annotation Field {} has name:{}", enclosedElement.getSimpleName(), tableField.value());
                namesJoiner.add(tableField.value());
                questionMarksJoiner.add("?");
            }

            cb = CodeBlock.of("\"$L\"", tableField.value());
            FieldSpec fs = FieldSpec.builder(String.class, tableField.value().toUpperCase(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(cb)
                    .build();
            consts.add(fs);

            logger.info("enclosed element:{}-{}",
                    enclosedElement.asType().toString(),
                    tableField
            );
        }
        String selectStringToSave = selectString.append(namesJoiner).append(" from ").append(tableName).append(" where ")
                .append(keysJoiner.toString()).toString();
        String insertStringToSave = insertString.append(namesJoiner.toString())
                .append(") values (")
                .append(questionMarksJoiner.toString())
                .append(")")
                .toString();

        logger.info("insert all string:{}", insertStringToSave);
        logger.info("select by cluster key string:{}", selectStringToSave);

        String className = element.getSimpleName().toString() + "Statements";

        cb = CodeBlock.of("\"$L\"", selectStringToSave);
        FieldSpec fsSelectByCluster = FieldSpec.builder(String.class, "SELECT_BY_CLUSTER_KEY", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(cb)
                .build();
        cb = CodeBlock.of("\"$L\"", insertStringToSave);
        FieldSpec fsInsertAll = FieldSpec.builder(String.class, "INSERT_ALL", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(cb)
                .build();
        FieldSpec fsInsertPS = FieldSpec.builder(ClassName.get("com.datastax.driver.core", "PreparedStatement"), "insertPreparedStatement")
                .addModifiers(Modifier.PRIVATE)
                .build();

        FieldSpec fsSelectByClusterKeyPS = FieldSpec.builder(ClassName.get("com.datastax.driver.core", "PreparedStatement"), "selectByClusterKeyPreparedStatement")
                .addModifiers(Modifier.PRIVATE)
                .build();

        List<FieldSpec> queriesFS = new ArrayList<>();
        NamedQuery[] queries = element.getAnnotation(NamedQueries.class).value();

        List<MethodSpec> gettersSpecList = new ArrayList<>();
        gettersSpecList.add(createGetterSpec("insertPreparedStatement"));
        gettersSpecList.add(createGetterSpec("selectByClusterKeyPreparedStatement"));

        StringBuilder preparedStatementsBuilder = new StringBuilder();
        preparedStatementsBuilder.append("this.insertPreparedStatement = session.prepare(INSERT_ALL);").append("\n");
        preparedStatementsBuilder.append("this.selectByClusterKeyPreparedStatement = session.prepare(SELECT_BY_CLUSTER_KEY);").append("\n");

        for (NamedQuery namedQuery : queries) {
            FieldSpec queryFS = FieldSpec.builder(ClassName.get("com.datastax.driver.core", "PreparedStatement"), namedQuery.name())
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            queriesFS.add(queryFS);
            gettersSpecList.add(createGetterSpec(namedQuery.name()));
            preparedStatementsBuilder.append("this.").append(namedQuery.name()).append(" = session.prepare(\"").append(namedQuery.value()).append("\");\n");
        }

        cb = CodeBlock.of(preparedStatementsBuilder.toString());
        ParameterSpec sessionParameter = ParameterSpec
                .builder(ClassName.get("com.datastax.driver.core", "Session"), "session")
                .build();

        MethodSpec constructor = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sessionParameter)
                .addCode(cb)
                .build();

        TypeSpec.Builder tsBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor)
                .addField(fsInsertAll)
                .addField(fsSelectByCluster)
                .addField(fsSelectByClusterKeyPS)
                .addField(fsInsertPS);

        for (FieldSpec fs : consts) {
            tsBuilder = tsBuilder.addField(fs);
        }
        for (FieldSpec fs : queriesFS) {
            tsBuilder = tsBuilder.addField(fs);
        }
        for (MethodSpec ms : gettersSpecList) {
            tsBuilder = tsBuilder.addMethod(ms);
        }

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className);
        JavaFile jf = JavaFile.builder(packageName, tsBuilder.build())
                .indent("    ")
                .build();
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            jf.writeTo(out);
        }
    }

    private MethodSpec createGetterSpec(String name) {
        CodeBlock cb = CodeBlock.of("return this." + name);
        return MethodSpec
                .methodBuilder("get" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length()))
                .returns(ClassName.get("com.datastax.driver.core", "PreparedStatement"))
                .addModifiers(Modifier.PUBLIC)
                .addStatement(cb)
                .build();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        final LoggerInitializer loggerInitializer = LoggerInitializer.getLoggerInitializer();
        loggerInitializer.setFile(new File(this.processingEnv.getOptions().get("logFile")));
        logger = loggerInitializer.createLogger(TestAnnotationProcess.class);
        logger.info("init()");
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
