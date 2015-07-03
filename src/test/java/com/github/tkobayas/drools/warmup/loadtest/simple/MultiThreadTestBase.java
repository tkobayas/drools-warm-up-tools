package com.github.tkobayas.drools.warmup.loadtest.simple;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import com.sample.Person;

public class MultiThreadTestBase {
    
    public static final int RULE_NUM = 1000;
    private static final String DRL_FILE_NAME = "LoadTest_" + RULE_NUM + "rules.drl";
//    private static final String DRL_FILE_NAME = "LoadTest_Join_" + RULE_NUM + "rules.drl";


    private final static int MAX_THREAD = 20;

    public static AtomicLong resultNum = new AtomicLong(0);

    public KieBase setupKieBase() throws Exception {
        
        System.out.println("---- setupKieBase start");
        long start = System.currentTimeMillis();

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/" + DRL_FILE_NAME, ks.getResources().newClassPathResource(DRL_FILE_NAME));
        ks.newKieBuilder(kfs).buildAll();
        KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        KieBase kBase = kContainer.getKieBase();
        
        System.out.println("---- setupKieBase end: elapsed time = " + (System.currentTimeMillis() - start) + "ms");
        
        return kBase;
    }

    public void runTest(final KieBase kBase) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD + 1);

        long totalStart = System.currentTimeMillis();

        final CountDownLatch startLatch = new CountDownLatch(MAX_THREAD);
        executor.execute(new ThroughputObserver(startLatch));

        for (int n = 0; n < MAX_THREAD; n++) {
            final int x = n;
            executor.execute(new Runnable() {

                public void run() {
                    KieSession kSession = kBase.newKieSession();
                    ArrayList resultList = new ArrayList();
                    kSession.setGlobal("resultList", resultList);
                    startLatch.countDown();

                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < (10000 / RULE_NUM); i++) {
                        for (int j = 0; j < RULE_NUM * 5; j += 5) {
                            Person p = new Person("John-" + x + "-" + i + "-" + j, j);
                            FactHandle factHandle = kSession.insert(p);
                            int fireNum = kSession.fireAllRules();
                            resultNum.addAndGet(1);
                            kSession.delete(factHandle);
                        }
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(300, TimeUnit.SECONDS);

        long totalElapsedTime = System.currentTimeMillis() - totalStart;
        System.out.println("total elapsed time = " + totalElapsedTime + "ms");
        System.out.println("throughput = " + ((double) resultNum.get() / totalElapsedTime) + " (num/ms)");

    }

}
