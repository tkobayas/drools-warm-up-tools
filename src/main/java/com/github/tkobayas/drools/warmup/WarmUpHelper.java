package com.github.tkobayas.drools.warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.drools.core.base.ClassObjectType;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.rule.constraint.ConditionEvaluator;
import org.drools.core.rule.constraint.MvelConstraint;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.concurrent.ExecutorProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A helper class to optimize kbase by warm-up.
 *
 * <p>
 * <h3>Usage example:</h3>
 * <p/>
 * <pre>
 * {@code
 * WarmUpHelper helper = new WarmUpHelper();
 * 
 * helper.analyze(kBase); // analyze your kbase first. Mandatory
 * 
 * helper.optimizeAlphaNodeConstraints(); // optimize constraints. Optional
 * 
 * Object[] facts = new Object[FACT_NUM];
 * for (int i = 0; i < FACT_NUM; i++) {
 *     facts[i] = new Person("John" + i, i * 5);
 * }
 * HashMap<String, Object> globalMap = new HashMap<String, Object>();
 * globalMap.put("resultList", new ArrayList<String>());
 * helper.warmUpWithFacts(facts, globalMap); // warm up with prepared facts which fire (most of) all rules. Optional
 * 
 * helper.reviewUnjittedMvelConstraint() // Logs constraints which are not Jitted. So helpful to prepare your warm-up facts. Optional
 * }
 * </pre>
 */
public class WarmUpHelper {

    private static final Logger logger = LoggerFactory.getLogger(WarmUpHelper.class);

    private static final long DEFAULT_COMPILE_THRESHOLD = 10000; // -XX:CompileThreshold

    private long compileThreshold = DEFAULT_COMPILE_THRESHOLD;

    private static final int WARMUP_LOOP_NUM = 20;

    private KieBase kbase;

    private KieBaseAnalyzer analyzer;

    public WarmUpHelper() {
        compileThreshold = Long.parseLong(System.getProperty("drools.warmup.compileThreshold", String.valueOf(DEFAULT_COMPILE_THRESHOLD)));
    }

    public void analyze(KieBase kbase) {
        analyze(kbase, false);
    }

    /**
     * Traverse kbase and store Mvelconstaints in the kbase
     * 
     * @param kbase
     * @param dump if true, dump Rete nodes
     */
    public void analyze(KieBase kbase, boolean dump) {

        logger.info("--- analyze started");
        long start = System.currentTimeMillis();

        this.kbase = kbase;
        analyzer = new KieBaseAnalyzer(dump);
        analyzer.traverseRete(kbase);

        logger.info("--- analyze finished : elapsed time = " + (System.currentTimeMillis() - start) + "ms");
    }

    public void optimizeAlphaNodeConstraints() throws InstantiationException, IllegalAccessException {
        optimizeAlphaNodeConstraints(true);
    }
    
    /**
     * 
     * Jit MvelConstaint which are associated with AlphaNode. Here "Jit" means that Drools generates a class for the constraint to be used instead of Mvel.
     * If you set forceJVMJit = true, the generated class will be Jitted by JVM so got further faster.
     * 
     * @param forceJVMJit
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void optimizeAlphaNodeConstraints(boolean forceJVMJit) throws InstantiationException, IllegalAccessException {

        // direct Jitting Mvelconstraints of AlphaNode
        logger.info("--- optimizeAlphaNodeConstraints started : forceJVMJit = " + forceJVMJit);
        long start = System.currentTimeMillis();

        KieSession ksession = kbase.newKieSession();
        Set<MvelConstraint> mvelConstraintSet = analyzer.getMvelConstraintSet();
        Map<MvelConstraint, MvelConstraintInfo> mvelConstraintInfoMap = analyzer.getMvelConstraintInfoMap();
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
                MvelConstraintUtils.setJitted(mvelConstraint);

                if (forceJVMJit) {
                    ConditionEvaluator jittedEvaluator = MvelConstraintUtils.getConditionEvaluator(mvelConstraint);
                    for (int i = 0; i < compileThreshold; i++) {
                        jittedEvaluator.evaluate(handle, (InternalWorkingMemory) ksession, null);
                    }
                }
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
        }

        printJitStats(mvelConstraintSet);

        logger.info("--- optimizeAlphaNodeConstraints finished : elapsed time = "
                + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Warm up kbase by ksession.insert/fireAllRules. You need to prepare facts to insert beforehand.
     * Warm-up efficiency would depend on how many constraints are evaluated and how many rules are fired.
     * The default number of loop is 20 to meet MvelConstarint.JIT_THRESOLD = 20.
     * 
     * @param facts
     * @param globalMap
     */
    public void warmUpWithFacts(Object[] facts, Map<String, Object> globalMap) {
        warmUpWithFacts(facts, globalMap, WARMUP_LOOP_NUM);
    }
    
    /**
     * Warm up kbase by ksession.insert/fireAllRules. You need to prepare facts to insert beforehand.
     * Warm-up efficiency would depend on how many constraints are evaluated and how many rules are fired.
     * 
     * @param facts
     * @param globalMap
     * @param warmupLoopNum
     */
    public void warmUpWithFacts(Object[] facts, Map<String, Object> globalMap, int warmupLoopNum) {
        // Warm-up with ksession.insert()
        logger.info("--- warmUpWithFacts ksession-run started");
        long start = System.currentTimeMillis();

        KieSession ksession = kbase.newKieSession();
        if (globalMap != null) {
            for (String key : globalMap.keySet()) {
                ksession.setGlobal(key, globalMap.get(key));
            }
        }
        for (int i = 0; i < warmupLoopNum; i++) {
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
        logger.info("--- warmUpWithFacts ksession-run finished : elapsed time = "
                + (System.currentTimeMillis() - start) + "ms");

        // Wait for jit threads
        logger.info("--- warmUpWithFacts jit-waiting started");
        long start2 = System.currentTimeMillis();
        long timeout = 5000;
        try {

            ThreadPoolExecutor executor = (ThreadPoolExecutor) ExecutorProviderFactory.getExecutorProvider()
                    .getExecutor();

            while (true) {
                logger.debug("activeCount = " + executor.getActiveCount() + ", taskCount = " + executor.getTaskCount()
                        + ", completedTaskCount = " + executor.getCompletedTaskCount());
                if (executor.getTaskCount() == executor.getCompletedTaskCount()
                        || (System.currentTimeMillis() - start2) > timeout) {
                    break;
                }
                Thread.sleep(500);
            }

        } catch (InterruptedException e) {
        }

        printJitStats(analyzer.getMvelConstraintSet());

        logger.info("--- warmUpWithFacts jit-waiting finished : elapsed time = "
                + (System.currentTimeMillis() - start2) + "ms");
    }

    private void printJitStats(Set<MvelConstraint> mvelConstraintSet) {
        int total = mvelConstraintSet.size();
        int jitted = 0;
        for (MvelConstraint mvelConstraint : mvelConstraintSet) {
            if (MvelConstraintUtils.isJitDone(mvelConstraint)) {
                jitted++;
            }
        }
        logger.info(jitted + " constrains are jitted out of " + total);
    }

    public void dumpMvelConstraint() {
        analyzer.dumpMvelConstraint();
    }

    /**
     * Logs constraints which are not Jitted.
     */
    public void reviewUnjittedMvelConstraint() {
        logger.info("--- reviewUnjittedMvelConstraint ---");
        Map<MvelConstraint, MvelConstraintInfo> mvelConstraintInfoMap = analyzer.getMvelConstraintInfoMap();
        for (MvelConstraint mvelConstraint : mvelConstraintInfoMap.keySet()) {
            if (MvelConstraintUtils.isJitDone(mvelConstraint)) {
                continue;
            }
            MvelConstraintInfo info = mvelConstraintInfoMap.get(mvelConstraint);
            logger.info(mvelConstraint.toString());
            // logger.info("    ObjectTypeNode = " + info.getOtn());
            // logger.info("    parent = " + info.getParent());
            logger.info("    parentNodeConstraints");
            Set<MvelConstraint> parentNodeConstraints = info.getParentNodeConstraints();
            for (MvelConstraint parentNodeConstraint : parentNodeConstraints) {
                // if (MvelConstraintUtils.isJitDone(parentNodeConstraint)) {
                // continue;
                // }
                logger.info("        -> " + parentNodeConstraint);
            }
        }
        logger.info("-----------------------------------");

    }
}
