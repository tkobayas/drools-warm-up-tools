package com.github.tkobayas.drools.warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.drools.core.rule.constraint.MvelConstraint;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.conf.RuleEngineOption;

/**
 * Not JUnit TestCase at this moment
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {

            System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            KieBaseConfiguration conf = ks.newKieBaseConfiguration();
            // conf.setOption(RuleEngineOption.RETEOO);
            KieBase kbase = kContainer.newKieBase(conf);

            // Optimize
            List<Object> factList = new ArrayList<Object>();
            // MvelConstraintOptimizer.optimize(kbase, factList);

            ReteDumper reteDumper = new ReteDumper();
            reteDumper.dumpRete(kbase);
            
            System.out.println();

            KieSession kSession = kbase.newKieSession();

            KieRuntimeLogger logger = ks.getLoggers().newFileLogger(kSession, "test");

            // go !
            Person john = new Person("John", 25);
            kSession.insert(john);
            Employee paul = new Employee("Paul", 23);
            kSession.insert(paul);
            Person george = new Person("George", 22);
            kSession.insert(george);
            Employee ringo = new Employee("Ringo", 25);
            kSession.insert(ringo);

            kSession.fireAllRules();

            logger.close();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
