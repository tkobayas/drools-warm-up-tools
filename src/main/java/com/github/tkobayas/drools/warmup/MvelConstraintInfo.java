package com.github.tkobayas.drools.warmup;

import org.drools.core.common.BaseNode;
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

    // Unique to mvelConstraint
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mvelConstraint == null) ? 0 : mvelConstraint.hashCode());
        return result;
    }

    // Unique to mvelConstraint
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MvelConstraintInfo other = (MvelConstraintInfo) obj;
        if (mvelConstraint == null) {
            if (other.mvelConstraint != null)
                return false;
        } else if (!mvelConstraint.equals(other.mvelConstraint))
            return false;
        return true;
    }

}
