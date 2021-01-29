package wncalculus.wnbag;

import wncalculus.bagexpr.ScalarProd;

/**
 * scalar product of arc-functions
 * @author lorenzo capra
 */
public final class TupleBagScalarProd extends ScalarProd<BagfunctionTuple> implements BagfunctionTuple{
    
    private TupleBagScalarProd(BagfunctionTuple arg, int k) {
        super(arg,k);
    }
    
    @Override
    public BagfunctionTuple buildOp(BagfunctionTuple arg) {
        return new TupleBagScalarProd(arg, k());
    }
    
    public static BagfunctionTuple factory (BagfunctionTuple arg, int k) {
        if (k == 1)
            return arg;
        
        if (k == 0)
            return new TupleBag(arg.getDomain(), arg.getCodomain());
        
        return arg instanceof WNtuple ? new TupleBag( k, arg) : new TupleBagScalarProd(arg, k);
    }
    
}
