package com.github.tkobayas.drools.warmup.loadtest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import com.github.tkobayas.drools.warmup.MvelConstraintOptimizer;
import com.sample.Employee;
import com.sample.Person;

/**
 * This is a sample class to launch a rule.
 */
public class MultiThreadTest {
    
    private final static int MAX_THREAD = 20;
    
    public static AtomicLong resultNum = new AtomicLong(0);

    @Test
    public void testRule() throws Exception {
        
        // load up the knowledge base
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.write("src/main/resources/LoadTest_500rules.drl", ks.getResources().newClassPathResource("LoadTest_500rules.drl"));
        ks.newKieBuilder( kfs ).buildAll();
        KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        final KieBase kBase = kContainer.getKieBase();
        
        //------------------------------------
        MvelConstraintOptimizer optimizer = new MvelConstraintOptimizer();
        optimizer.analyze(kBase);
        optimizer.optimizeAlphaNodeConstraints();
        Person p = new Person("John", Integer.MAX_VALUE);
        Object[] facts = new Object[]{p};
        HashMap<String, Object> globalMap = new HashMap<String, Object>();
        globalMap.put("resultList", new ArrayList<String>());
        optimizer.warmUpWithFacts(facts, globalMap);
        //------------------------------------
        
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

                    for (int i = 0; i < 1000; i++) {
                        long start = System.currentTimeMillis();
                        for (int j = 0; j < 2500; j++) {
                            Person p = new Person("John-" + x + "-" + i + "-" + j, j);
                            FactHandle factHandle = kSession.insert(p);
                            int fireNum = kSession.fireAllRules();
                            resultNum.addAndGet(1); // 1 is good for measuring throughput in this case. And fireNum is supposed to be '1' with the rules anyway

                            kSession.delete(factHandle);
                        }
//                        System.out.println("### thread " + x + ", round " + i + ", elapsed time = "
//                                + (System.currentTimeMillis() - start) + "ms");
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(300, TimeUnit.SECONDS);
        
        long totalElapsedTime = System.currentTimeMillis() - totalStart;
        System.out.println("total elapsed time = " + totalElapsedTime + "ms");
        System.out.println("throughput = " + ((double)resultNum.get() / totalElapsedTime) + " (num/ms)");

        assertTrue(true);

    }

}
