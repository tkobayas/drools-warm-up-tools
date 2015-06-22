package com.github.tkobayas.drools.warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.base.ClassObjectType;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.rule.constraint.ConditionEvaluator;
import org.drools.core.rule.constraint.MvelConstraint;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

public class MvelConstraintOptimizer {

    private KieBase kbase;

    private MvelConstraintCollector collector;

    public MvelConstraintOptimizer() {
    }

    public void analyze(KieBase kbase) {

        System.out.println("--- analyze started");
        long start = System.currentTimeMillis();

        this.kbase = kbase;
        collector = new MvelConstraintCollector();
        collector.traverseRete(kbase);

        System.out.println("--- analyze finished : elapsed time = " + (System.currentTimeMillis() - start) + "ms");
    }

    public void optimizeAlphaNodeConstraints() throws InstantiationException, IllegalAccessException {

        // direct Jitting Mvelconstraints of AlphaNode
        System.out.println("--- optimizeAlphaNodeConstraints started");
        long start = System.currentTimeMillis();

        KieSession ksession = kbase.newKieSession();
        Set<MvelConstraint> mvelConstraintSet = collector.getMvelConstraintSet();
        Map<MvelConstraint, MvelConstraintInfo> mvelConstraintInfoMap = collector.getMvelConstraintInfoMap();
        int id = 0;
        for (MvelConstraint mvelConstraint : mvelConstraintSet) {
            MvelConstraintInfo mvelConstraintInfo = mvelConstraintInfoMap.get(mvelConstraint);
            if (!(mvelConstraintInfo.getParent() instanceof AlphaNode)) {
                continue;
            }
            Class<?> factClass = ((ClassObjectType) mvelConstraintInfo.getOtn().getObjectType()).getClassType();
            Object fact = factClass.newInstance();
            DefaultFactHandle handle = new DefaultFactHandle(id++, fact);
            MvelConstraintUtils.createMvelConditionEvaluator(mvelConstraint, (InternalWorkingMemory) ksession);
            ConditionEvaluator conditionEvaluator = MvelConstraintUtils.getConditionEvaluator(mvelConstraint);
            try {
                conditionEvaluator.evaluate(handle, (InternalWorkingMemory) ksession, null);
                MvelConstraintUtils.executeJitting(mvelConstraint, handle, (InternalWorkingMemory) ksession, null);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        printJitStats(mvelConstraintSet);

        System.out.println("--- optimizeAlphaNodeConstraints finished : elapsed time = "
                + (System.currentTimeMillis() - start) + "ms");
    }

    public void warmUpWithFacts(Object[] facts, Map<String, Object> globalMap) {
        // Warm-up with ksession.insert()
        System.out.println("--- warmUpWithFacts started");
        long start = System.currentTimeMillis();

        KieSession ksession = kbase.newKieSession();
        for (String key : globalMap.keySet()) {
            ksession.setGlobal(key, globalMap.get(key));
        }
        for (int i = 0; i < 20; i++) {
            List<FactHandle> handleList = new ArrayList<FactHandle>();
            for (Object fact : facts) {
                FactHandle handle = ksession.insert(fact);
                handleList.add(handle);
            }
            ksession.fireAllRules();
            for (FactHandle handle : handleList) {
                ksession.delete(handle);
            }
        }
        ksession.dispose();

        // Wait for jit threads
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        printJitStats(collector.getMvelConstraintSet());
        
        System.out.println("--- warmUpWithFacts finished : elapsed time = "
                + (System.currentTimeMillis() - start) + "ms");
    }

    private void printJitStats(Set<MvelConstraint> mvelConstraintSet) {
        int total = mvelConstraintSet.size();
        int jitted = 0;
        for (MvelConstraint mvelConstraint : mvelConstraintSet) {
            if (MvelConstraintUtils.isJitDone(mvelConstraint)) {
                jitted++;
            }
        }
        System.out.println(jitted + " constrains are jitted out of " + total);
    }

    public void dumpMvelConstraint() {
        collector.dumpMvelConstraint();
    }
}
