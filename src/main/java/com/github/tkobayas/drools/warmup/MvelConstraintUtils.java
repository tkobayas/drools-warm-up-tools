package com.github.tkobayas.drools.warmup;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.common.BaseNode;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.Sink;
import org.drools.core.rule.constraint.ConditionEvaluator;
import org.drools.core.rule.constraint.MvelConditionEvaluator;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.Constraint;
import org.kie.api.KieBase;

public class MvelConstraintUtils {

    public static boolean isJitDone(MvelConstraint mvelConstraint) {
        ConditionEvaluator conditionEvaluator = null;
        try {
            Field field = MvelConstraint.class.getDeclaredField("conditionEvaluator");
            field.setAccessible(true);
            conditionEvaluator = (ConditionEvaluator)field.get(mvelConstraint);
            //System.out.println(conditionEvaluator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conditionEvaluator != null && !(conditionEvaluator instanceof MvelConditionEvaluator)) {
            return true; // Jitted class would be like ConditionEvaluator5ff1bb4580d54191aab1e7897463b4b4.class
        } else {
            return false;
        }
    }
    
    public static int getInvocationCounter(MvelConstraint mvelConstraint) {
        AtomicInteger invocationCounter = null;
        try {
            Field field = MvelConstraint.class.getDeclaredField("invocationCounter");
            field.setAccessible(true);
            invocationCounter = (AtomicInteger)field.get(mvelConstraint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return invocationCounter.get(); // max is 21
    }
}
