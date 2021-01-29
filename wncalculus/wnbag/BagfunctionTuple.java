package wncalculus.wnbag;

import java.util.Map;
import wncalculus.bagexpr.BagExpr;
import wncalculus.expr.Domain;
import wncalculus.expr.Expression;
import wncalculus.expr.ParametricExpr;
import wncalculus.guard.Guard;

/**
 * the super type of legacy SN (WN) arc-functions
 * tuple components are linear combinations of elementary (class-)functions
 * @author lorenzo capra
 */
public interface BagfunctionTuple extends SNfunctionTuple<LinearComb>, BagExpr {
    
    
    @Override
    default BagfunctionTuple scalarProdFactory(BagExpr arg, int k) {
        return TupleBagScalarProd.factory((BagfunctionTuple) arg, k); 
    }
    
    @Override
    default TupleBagTranspose buildTransp() {
        return new TupleBagTranspose(this);
    }
    
    @Override
    default Class<BagfunctionTuple> bagType() {
       return /*WNtuple.class*/type(); //a seconda che si usi TupleBag o TupleBag (scelta attuale)   
    }
    
    @Override
    default Class<BagfunctionTuple> type() {
        return BagfunctionTuple.class;
    }
    
    @Override
    default TupleBag build(Map<? extends ParametricExpr, Integer> m) {
        return new TupleBag((Map<? extends BagfunctionTuple, Integer>)m);
    }
    
     @Override
     default TupleBag build(Domain d, Domain cd) {
        return new TupleBag(d,cd);
    }
     
    @Override
    default BagfunctionTuple buildGuardedExpr(Guard f, Expression e, Guard g) {
        return new GuardedArcFunction(f, (BagfunctionTuple) e, g);
    }
   
}
