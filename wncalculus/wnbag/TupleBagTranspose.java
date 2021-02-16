package wncalculus.wnbag;

import wncalculus.bagexpr.BagTranspose;

/**
 * this class defines the transpose of bag-expressions built over legacy SN arc-functions
 * @author lorenzo capra
 */
public final class TupleBagTranspose extends BagTranspose<BagfunctionTuple,WNtuple> implements BagfunctionTuple {
    
    public TupleBagTranspose (BagfunctionTuple a) {
        super(a);
    }

    @Override
    public TupleBagTranspose buildOp(BagfunctionTuple arg) {
        return new TupleBagTranspose(arg);
    }
    
}
