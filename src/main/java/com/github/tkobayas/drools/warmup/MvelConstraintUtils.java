package com.github.tkobayas.drools.warmup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.rule.constraint.ConditionEvaluator;
import org.drools.core.rule.constraint.MvelConditionEvaluator;
import org.drools.core.rule.constraint.MvelConstraint;

/**
 * 
 * Utility class including static methods which tweak MvelConstraint private members.
 * 
 * You would need to take care of security policy if enabled.
 *
 */
public class MvelConstraintUtils {

    public static boolean isJitDone(MvelConstraint mvelConstraint) {
        ConditionEvaluator conditionEvaluator = null;
        try {
            Field field = MvelConstraint.class.getDeclaredField("conditionEvaluator");
            field.setAccessible(true);
            conditionEvaluator = (ConditionEvaluator)field.get(mvelConstraint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conditionEvaluator != null && !(conditionEvaluator instanceof MvelConditionEvaluator)) {
            return true; // Jitted class would be like ConditionEvaluator5ff1bb4580d54191aab1e7897463b4b4.class
        } else {
            return false;
        }
    }
    
    public static void setJitted(MvelConstraint mvelConstraint) {
        try {
            Field field = MvelConstraint.class.getDeclaredField("jitted");
            field.setAccessible(true);
            field.setBoolean(mvelConstraint, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static ConditionEvaluator getConditionEvaluator(MvelConstraint mvelConstraint) {
        ConditionEvaluator conditionEvaluator = null;
        try {
            Field field = MvelConstraint.class.getDeclaredField("conditionEvaluator");
            field.setAccessible(true);
            conditionEvaluator = (ConditionEvaluator)field.get(mvelConstraint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conditionEvaluator;
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

    
    public static void createMvelConditionEvaluator(MvelConstraint mvelConstraint, InternalWorkingMemory workingMemory) {
        try {
            Method method = MvelConstraint.class.getDeclaredMethod("createMvelConditionEvaluator", InternalWorkingMemory.class);
            method.setAccessible(true);
            method.invoke(mvelConstraint, workingMemory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeJitting(MvelConstraint mvelConstraint, InternalFactHandle handle, InternalWorkingMemory workingMemory, LeftTuple leftTuple) {
        try {
            Method method = MvelConstraint.class.getDeclaredMethod("executeJitting", InternalFactHandle.class, InternalWorkingMemory.class, LeftTuple.class);
            method.setAccessible(true);
            method.invoke(mvelConstraint, handle, workingMemory, leftTuple);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
