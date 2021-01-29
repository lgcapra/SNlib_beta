package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.Guard;
import wncalculus.wnbag.TupleBag;
import wncalculus.wnbag.WNtuple;
import wncalculus.wnbag.BagfunctionTuple;


/**
 * This class represents the support of WN function-tuples mapping on multi-sets
 * @author Lorenzo Capra
 * dovrebbe implementare <tt>SingleArg<SetArcFunction,FunctionTuple></tt>
 */

public final class TupleSupport implements FunctionTuple, SingleArg<BagfunctionTuple, FunctionTuple> {
    
    private final BagfunctionTuple func;
    private boolean simplified;
    
    /**
     * base constructor: creates the support of a bag-expression
     * @param b a bag-expression
     */
    public TupleSupport(BagfunctionTuple b) {
        this.func = b;
    }


    //new version: the support is a unary operator
    @Override
    public FunctionTuple specSimplify() {
        if (this.func instanceof TupleBag) {
            TupleBag bag = (TupleBag)this.func;
            if (bag.isEmpty())
                return getFalse();
            
            if (bag.elementary()) {
                Set<? extends BagfunctionTuple> support = bag.properSupport();
                Set<Tuple> tuples = new HashSet<>();
                support.forEach(t -> {
                    WNtuple wt = (WNtuple) t;
                    Guard g = wt.guard(), f =wt.filter();
                    SortedMap<ColorClass,List<? extends SetFunction>> m = new TreeMap<>();
                    wt.getHomSubTuples().entrySet().forEach(et -> {
                        List<SetFunction> l = new ArrayList<>();
                        et.getValue().forEach(lc -> { l.add(lc.toSetFunction()); });
                        m.put(et.getKey(), l);
                    });
                    tuples.add(new Tuple(f, getCodomain(), m, g, getDomain()));
                });

                return TupleSum.factory(tuples,false); //add disjoined() to log bag?
            }
        }
        
        return this;
    }

    @Override
    public boolean differentFromZero() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return this.func.isConstant();
    }
    
    
   @Override
   public boolean equals (Object o) {
        return SingleArg.super.isEqual(o);
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.func);
        
        return hash;
    }
    
    @Override
    public String toString() {
        return "{"+this.func+'}';
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return this.func.splitDelimiters();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
   
    @Override
    public TupleSupport buildOp(BagfunctionTuple arg) {
       return new TupleSupport(arg);    
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BagfunctionTuple  getArg() {
        return this.func;
    }

    @Override
    public boolean isInvolution() {
        return true;
    }


}

