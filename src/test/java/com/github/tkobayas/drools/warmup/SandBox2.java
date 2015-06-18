package com.github.tkobayas.drools.warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.drools.core.io.impl.ClassPathResource;
import org.drools.core.rule.constraint.MvelConstraint;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.ResourceType;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.builder.conf.RuleEngineOption;

import com.sample.Employee;
import com.sample.Person;

/**
 * Not JUnit TestCase at this moment
 */
public class SandBox2 {

    public static final void main(String[] args) {
        try {

            System.setProperty("drools.dump.dir", "/home/tkobayas/tmp");

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/Sample2.drl", ks.getResources().newClassPathResource("Sample2.drl"));
            ks.newKieBuilder( kfs ).buildAll();
            KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
            KieBase kbase = kContainer.getKieBase();

            List<Object> factList = new ArrayList<Object>();

            MvelConstraintCollector collector = new MvelConstraintCollector();
            collector.traverseRete(kbase);
            
            System.out.println();

            KieSession kSession = kbase.newKieSession();
            ArrayList resultList = new ArrayList();
            kSession.setGlobal("resultList", resultList);
            
            // go !
            Person john = new Person("John", 0);
            kSession.insert(john);
            Person paul = new Person("Paul", 500);
            kSession.insert(paul);

            int fired = kSession.fireAllRules();
            System.out.println("fired = " + fired);
            
            collector.listMvelConstraint();


        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
