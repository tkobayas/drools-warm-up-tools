package com.github.tkobayas.drools.warmup.sandbox;

import java.util.ArrayList;
import java.util.HashMap;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;

import com.github.tkobayas.drools.warmup.WarmUpHelper;
import com.sample.Employee;
import com.sample.Person;

/**
 * Not JUnit TestCase at this moment
 */
public class SandBox4_2 {

    public static final void main(String[] args) {
        try {

            System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

            System.out.println("--- kbase build started");
            long start = System.currentTimeMillis();
            
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/Sample4.drl", ks.getResources().newClassPathResource("Sample4.drl"));
            ks.newKieBuilder( kfs ).buildAll();
            KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            KieBase kbase = kContainer.getKieBase();
            
            System.out.println("--- kbase build finished : elapsed time = "
                    + (System.currentTimeMillis() - start) + "ms");
            
            WarmUpHelper helper = new WarmUpHelper();
            helper.analyze(kbase, false);
            
            Person p = new Person("John", Integer.MAX_VALUE);
            Employee e = new Employee("Paul", 100);
            Object[] facts = new Object[]{p, e};
            HashMap<String, Object> globalMap = new HashMap<String, Object>();
            globalMap.put("resultList", new ArrayList<String>());
            helper.warmUpWithFacts(facts, globalMap);
            
            //helper.dumpMvelConstraint();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
