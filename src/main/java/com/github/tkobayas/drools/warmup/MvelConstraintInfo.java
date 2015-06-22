package com.github.tkobayas.drools.warmup;

import org.drools.core.common.BaseNode;
import org.drools.core.reteoo.JoinNode;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.rule.constraint.MvelConstraint;

/**
 * 
 * Wrapper class of MvelConstraint to hold associated information
 *
 */
public class MvelConstraintInfo {

    private MvelConstraint mvelConstraint;
    
    private ObjectTypeNode otn; // root otn
    
    private BaseNode parent;

    public MvelConstraintInfo(MvelConstraint mvelConstraint, ObjectTypeNode otn, BaseNode parent) {
        this.mvelConstraint = mvelConstraint;
        this.otn = otn;
        this.parent = parent;
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
}
