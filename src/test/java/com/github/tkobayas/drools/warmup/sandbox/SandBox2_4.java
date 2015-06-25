package com.github.tkobayas.drools.warmup.sandbox;

import java.util.ArrayList;
import java.util.HashMap;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;

import com.github.tkobayas.drools.warmup.MvelConstraintOptimizer;
import com.sample.Person;

/**
 * Not JUnit TestCase at this moment
 */
public class SandBox2_4 {

    public static final void main(String[] args) {
        try {

            System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/Sample2.drl", ks.getResources().newClassPathResource("Sample2.drl"));
            ks.newKieBuilder( kfs ).buildAll();
            KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            KieBase kbase = kContainer.getKieBase();
            
            MvelConstraintOptimizer optimizer = new MvelConstraintOptimizer();
            optimizer.analyze(kbase);
            
            Person p1 = new Person("John", 0);
            Person p2 = new Person("Paul", 500);
            Object[] facts = new Object[]{p1, p2};
            HashMap<String, Object> globalMap = new HashMap<String, Object>();
            globalMap.put("resultList", new ArrayList<String>());
            optimizer.warmUpWithFacts(facts, globalMap);
            
            optimizer.dumpMvelConstraint();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
