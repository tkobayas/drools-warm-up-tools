package com.github.tkobayas.drools.warmup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.drools.core.common.BaseNode;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.BetaNodeFieldConstraint;
import org.drools.core.spi.Constraint;

/**
 * 
 * Wrapper class of MvelConstraint to hold associated information
 *
 */
public class MvelConstraintInfo {

    private MvelConstraint mvelConstraint;
    
    private ObjectTypeNode otn; // root otn
    
    private BaseNode parent;
    
    private Set<MvelConstraint> parentNodeConstraints;

    public MvelConstraintInfo(MvelConstraint mvelConstraint, ObjectTypeNode otn, BaseNode parent) {
        this.mvelConstraint = mvelConstraint;
        this.otn = otn;
        this.parent = parent;
        this.parentNodeConstraints = new HashSet<MvelConstraint>();
    }

    public MvelConstraint getMvelConstraint() {
        return mvelConstraint;
    }

    public void setMvelConstraint(MvelConstraint mvelConstraint) {
        this.mvelConstraint = mvelConstraint;
    }

    public ObjectTypeNode getOtn() {
        return otn;
    }

    public void setOtn(ObjectTypeNode otn) {
        this.otn = otn;
    }

    public BaseNode getParent() {
        return parent;
    }

    public void setParent(BaseNode parent) {
        this.parent = parent;
    }
    
    

    public Set<MvelConstraint> getParentNodeConstraints() {
        return parentNodeConstraints;
    }

    public void setParentNodeConstraints(Set<MvelConstraint> parentNodeConstraints) {
        this.parentNodeConstraints = parentNodeConstraints;
    }

    public void addParentNodeConstraints(ArrayDeque<BaseNode> parentNodeStack) {
        for (BaseNode node : parentNodeStack) {
            if (node instanceof AlphaNode) {
                Constraint constraint = ((AlphaNode) node).getConstraint();
                if (constraint instanceof MvelConstraint) {
                    parentNodeConstraints.add((MvelConstraint)constraint);
                }
            } else if (node instanceof BetaNode) {
                BetaNodeFieldConstraint[] constraints = ((BetaNode) node).getConstraints();
                for (BetaNodeFieldConstraint constraint : constraints) {
                    if (constraint instanceof MvelConstraint) {
                        parentNodeConstraints.add((MvelConstraint)constraint);
                    }
                }
            }
        }
    }
}
