package pt.fabm.schedulers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class SchedulersTest {
    Observable<String> simple() {
        return Observable.range(1, 10).map(Object::toString);
    }

    private static void log(String msg) {
        String threadName = Thread.currentThread().getName();
        System.out.println(System.currentTimeMillis() + " | " + threadName + " | " + msg);
    }


    private ThreadFactory threadFactory(String pattern) {
        return new ThreadFactoryBuilder().setNameFormat(pattern).build();
    }

    @Test
    public void ymlTest() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Yaml yaml = new Yaml();
        InputStream is = getClass().getResourceAsStream("/processes.yml");
        Map<String, Object> map = yaml.load(is);

        Map<String, Object> beans = Map.class.cast(map.get("beans"));
        Map<String, Object> processes = Map.class.cast(map.get("processes"));

        Assert.assertEquals(3, beans.size());

        for (Map.Entry<String, Object> bean : beans.entrySet()) {
            System.out.println("bean name:" + bean.getKey());
            System.out.println("bean content:" + bean.getValue());
            System.out.println("bean content:" + bean.getValue().getClass());
            System.out.println();
        }


        System.out.println();

    }

    @Test
    public void testProcesses(){

    }

    @Test
    public void testForkJoin() throws InterruptedException, IOException {
        Logger logger = LoggerFactory.getLogger("testForkJoin");
        ExecutorService poolA = newFixedThreadPool(10, threadFactory("Sched-A-%d"));
        Scheduler schedulerA = Schedulers.from(poolA);

        LoggerFactory.getLogger("init").info("before");
        Observable<Integer> fc1 = Observable.just(1)
                .observeOn(schedulerA)
                .map(e -> {
                    Thread.sleep(3000);
                    logger.info("fc1");
                    return 1;
                });

        Observable<Integer> fc2 = Observable.just(1)
                .observeOn(schedulerA)
                .map(e -> {
                    Thread.sleep(3000);
                    logger.info("fc2");
                    return 2;
                });
        Observable<Integer> fc3 = Observable.just(1)
                .observeOn(schedulerA)
                .map(e -> {
                    Thread.sleep(3000);
                    logger.info("fc3");
                    return 3;
                });

        io.reactivex.functions.Function<Object[], Integer> zipper = arr -> {
            LoggerFactory.getLogger("zipper").info(Arrays.deepToString(arr));
            return 2;
        };

        //Observable<Integer> obs = Observable.zipIterable(Arrays.asList(fc1, fc2, fc3), zipper, false, 2);
        Observable<Integer> obs = Observable.merge(fc1, fc2, fc3);


        io.reactivex.functions.Function<Map.Entry, String> mmm = m->{
            return "var"+m.getKey();
        };
        Single<Map<String, AbstractMap.SimpleImmutableEntry>> obs1 = obs.map(m -> new AbstractMap
                .SimpleImmutableEntry(m, m.toString())).toMap(mmm);

        Yaml yaml = new Yaml();
        InputStream is = getClass().getResourceAsStream("/processes.yml");
        Map<String, Object> map = yaml.load(is);

        Map<String, Object> beans = Map.class.cast(map.get("beans"));

        SimpleTemplateEngine ste = new SimpleTemplateEngine();
        Template tpl = ste.createTemplate(new StringReader(beans.get("t2").toString()));

        Single<String> obs2 = obs1.map(m -> {
            return tpl.make(m).toString();
        });

        logger.info("final " + obs2.blockingGet());
    }

    @Test
    public void scheduleTest() throws InterruptedException {

        ExecutorService poolA = newFixedThreadPool(10, threadFactory("Sched-A-%d"));
        Scheduler schedulerA = Schedulers.from(poolA);

        log("Starting");
        Observable<String> obs = simple();
        log("Created");
        obs
                //.observeOn(schedulerA)
                .map(x -> {
                    log("mapping");
                    return x;
                })
                .filter(x -> true)
                .subscribe(x ->
                                log("Got " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed")
                );
        log("Exiting");


        System.out.println();
        log("Starting");
        obs = simple();
        log("Created");
        obs
                .observeOn(schedulerA)
                .map(x -> {
                    log("mapping");
                    return x;
                })
                .filter(x -> true)
                .subscribe(x ->
                                log("Got " + x),
                        Throwable::printStackTrace,
                        () -> log("Completed")
                );
        log("Exiting");
    }

    @Test
    public void scheduleTest2() {
        ExecutorService poolA = newFixedThreadPool(3, threadFactory("Sched-A-%d"));
        Scheduler schedulerA = Schedulers.from(poolA);

        Logger logger = LoggerFactory.getLogger("sh2");
        io.reactivex.functions.Function<Integer, Observable<Integer>> fm = e -> {
            Observable<Integer> obs = Observable.just(e).observeOn(schedulerA).map(i -> {
                logger.info("executing inner double");
                return i * 2;
            });
            return obs;
        };
        Iterable<Integer> iter = Observable.range(1, 10)
                .flatMap(fm)
                .observeOn(Schedulers.trampoline())
                .map((Integer e) -> {
                    logger.info("executing double");
                    return e * 2;
                }).blockingIterable();
        for (Integer i : iter) {
            logger.info("i:{}", i);
        }
    }

}
