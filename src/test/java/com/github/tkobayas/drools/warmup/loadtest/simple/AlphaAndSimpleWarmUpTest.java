package com.github.tkobayas.drools.warmup.loadtest.simple;

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
public class AlphaAndSimpleWarmUpTest extends MultiThreadTestBase {
    
    @Test
    public void testRule() throws Exception {

        final KieBase kBase = setupKieBase();
        
        //------------------------------------
        WarmUpHelper helper = new WarmUpHelper();
        helper.analyze(kBase);
        helper.optimizeAlphaNodeConstraints();
        Person p = new Person("John", Integer.MAX_VALUE);
        Object[] facts = new Object[]{p};
        HashMap<String, Object> globalMap = new HashMap<String, Object>();
        globalMap.put("resultList", new ArrayList<String>());
        helper.warmUpWithFacts(facts, globalMap);
        //------------------------------------
        
        runTest(kBase);
        
        assertTrue(true);

    }

}
