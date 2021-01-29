package wncalculus.wnbag;

import wncalculus.bagexpr.BagComp;

/**
 * this class defines the composition between SN arc-function expressions
 * @author lorenzo capra
 */
public final class TupleBagComp extends BagComp<BagfunctionTuple> implements BagfunctionTuple {

    public TupleBagComp(BagfunctionTuple left, BagfunctionTuple right) {
        super(left,right);
    }
    
    
    @Override
    public TupleBagComp buildOp(BagfunctionTuple left, BagfunctionTuple right) {
        return new TupleBagComp(left,right);
    }
    
    /**
     * treats the base case of composition between<tt>WNtuples</tt>,
     * by invoking the corresponding method
     * @return 
     */
    @Override
    public BagfunctionTuple specSimplify() {
        BagfunctionTuple res = null;
        if (left() instanceof WNtuple && ((WNtuple) left()).filter() == null && right() instanceof WNtuple) 
            res = ((WNtuple)left()).tupleCompose((WNtuple) right());
       
        return res == null ? this : res;
    }
    
}
