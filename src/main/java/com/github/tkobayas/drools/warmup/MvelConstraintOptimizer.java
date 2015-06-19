package com.github.tkobayas.drools.warmup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.lang.dsl.DSLMapParser.meta_section_return;
import org.drools.core.base.ClassObjectType;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftTupleSink;
import org.drools.core.reteoo.LeftTupleSinkPropagator;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSinkPropagator;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.constraint.ConditionEvaluator;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.Constraint;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

public class MvelConstraintOptimizer {
    
    private MvelConstraintCollector collector;

    public MvelConstraintOptimizer() {
    }
    
    public void optimize(KieBase kbase) throws InstantiationException, IllegalAccessException {
        
        collector = new MvelConstraintCollector();
        collector.traverseRete(kbase);
        
        // Set up facts to insert
        ArrayList<Class> factClassList = new ArrayList<Class>();
        
        KnowledgeBaseImpl kbaseImpl = (KnowledgeBaseImpl)kbase;
        Collection<TypeDeclaration> typeDeclarations = kbaseImpl.getTypeDeclarations();
        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            Class<?> typeClass = typeDeclaration.getTypeClass();
            factClassList.add(typeClass);
        }
        
        // Warm-up with direct Jitting
        System.out.println("--- Start warming-up");
        
        KieSession ksession = kbase.newKieSession();
        Set<MvelConstraintInfo> mvelConstraintInfoSet = collector.getMvelConstraintInfoSet();
        int id = 0;
        for (MvelConstraintInfo mvelConstraintInfo : mvelConstraintInfoSet) {
            MvelConstraint mvelConstraint = mvelConstraintInfo.getMvelConstraint();
            Class<?> factClass = ((ClassObjectType)mvelConstraintInfo.getOtn().getObjectType()).getClassType();
            Object fact = factClass.newInstance();
            DefaultFactHandle handle = new DefaultFactHandle(id++, fact);
            MvelConstraintUtils.createMvelConditionEvaluator(mvelConstraint, (InternalWorkingMemory)ksession);
            ConditionEvaluator conditionEvaluator = MvelConstraintUtils.getConditionEvaluator(mvelConstraint);
            try {
                conditionEvaluator.evaluate(handle, (InternalWorkingMemory) ksession, null);
                MvelConstraintUtils.executeJitting(mvelConstraint, handle, (InternalWorkingMemory)ksession, null);
            } catch (Exception e) {
                System.out.println(e);
            }
            
        }
        
        
        // Warm-up with ksession.insert()
//        KieSession ksession = kbase.newKieSession();
//        Map<String, Class<?>> globals = kbaseImpl.getGlobals();
//        for (String key : globals.keySet()) {
//            Class<?> globalClass = globals.get(key);
//            Object global = globalClass.newInstance();
//            ksession.setGlobal(key, global);
//        }
//        for (int i = 0; i < 20; i++) {
//            for (Class factClass : factClassList) {
//                Object fact = factClass.newInstance();
//                ksession.insert(fact);
//            }
//            ksession.fireAllRules();
//        }
//        ksession.dispose();
        
        // Wait for jit threads
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//        }
        
        // Result
        int total = mvelConstraintInfoSet.size();
        int jitted = 0;
        for (MvelConstraintInfo mvelConstraintInfo : mvelConstraintInfoSet) {
            if (MvelConstraintUtils.isJitDone(mvelConstraintInfo.getMvelConstraint())) {
                jitted++;
            }
        }
        System.out.println(jitted + " constrains are jitted out of " + total );
    }
    
    public void dumpMvelConstraint() {
        collector.dumpMvelConstraint();
    }
}
