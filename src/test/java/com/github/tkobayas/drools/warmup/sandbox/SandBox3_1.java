package com.github.tkobayas.drools.warmup.sandbox;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;

import com.github.tkobayas.drools.warmup.MvelConstraintOptimizer;

/**
 * Not JUnit TestCase at this moment
 */
public class SandBox3_1 {

    public static final void main(String[] args) {
        try {

            System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/Sample3.drl", ks.getResources().newClassPathResource("Sample3.drl"));
            ks.newKieBuilder( kfs ).buildAll();
            KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            KieBase kbase = kContainer.getKieBase();
            
            MvelConstraintOptimizer optimizer = new MvelConstraintOptimizer();
            optimizer.analyze(kbase);
            optimizer.optimizeAlphaNodeConstraints();
            //optimizer.dumpMvelConstraint();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
