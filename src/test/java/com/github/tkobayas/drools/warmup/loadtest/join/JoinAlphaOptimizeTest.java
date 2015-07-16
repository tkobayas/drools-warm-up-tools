package com.github.tkobayas.drools.warmup.loadtest.join;

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

import com.github.tkobayas.drools.warmup.WarmUpHelper;
import com.sample.Employee;
import com.sample.Person;

/**
 * This is a sample class to launch a rule.
 */
public class JoinAlphaOptimizeTest extends JoinMultiThreadTestBase {
    
    @Test
    public void testRule() throws Exception {
        
        //System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

        final KieBase kBase = setupKieBase();
        
        //------------------------------------
        WarmUpHelper helper = new WarmUpHelper();
        helper.analyze(kBase);
        helper.optimizeAlphaNodeConstraints();
        //------------------------------------
        
        runTest(kBase);
        
        assertTrue(true);

    }

}
