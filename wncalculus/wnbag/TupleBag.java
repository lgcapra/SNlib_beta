package wncalculus.wnbag;

import java.util.*;
import wncalculus.bagexpr.AbstractBag;
import wncalculus.bagexpr.Bag;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.util.Util;

/**
 *
 * this class represents the base form of legacy SN arc-functions
 * @author lorenzo capra
 */
public final class TupleBag extends AbstractBag<BagfunctionTuple> implements BagfunctionTuple {
    
    private Boolean elementary;//cache
    private Integer card; //cache
    
    public TupleBag(Domain dom, Domain codom) {
        super(dom,codom);
    }
    
    public TupleBag(Map<? extends BagfunctionTuple, Integer> m) {
        super(m);
    }
    
    public TupleBag (Collection<? extends BagfunctionTuple> c) {
        super(Util.asMap(c));
    }
    
    public TupleBag (BagfunctionTuple ... l) {
        super(Arrays.asList(l));
    }
    
    public TupleBag (int k, BagfunctionTuple f) {
        super(Util.singleMap(f, k));
    
    }
    
    @Override
    public Integer card() {
        if (isEmpty())
            return 0;
        
        if (this.card!= null)
            return this.card;
        
        if (! isElementarySum())
            return null;
        
        Integer mycard = 0;
        for (Map.Entry<? extends BagfunctionTuple, Integer> x : asMap().entrySet()) {
            Integer xcard = x.getKey().card();
            if (xcard == null)
                return null;
            mycard *= xcard * x.getValue();
        }
           
        return card = mycard; //return and storein the cache
    }
    
    /**
     * 
     * @return <tt>true</tt> if and only if <tt>this</tt> bag just contains <tt>WNtuple</tt>s
     */
    public boolean elementary() {
        if (elementary == null)
            elementary = Util.checkAll(support(), WNtuple.class::isInstance);
        
        return elementary;
    }
   
    /**
     * 
     * @return <tt>true</tt> if and only if <tt>this</tt> bag just contains <tt>WNtuple</tt>s
     * and is not empty
     */
    @Override
    public boolean isElementarySum() {
        return !isEmpty() && elementary();
    }
    
    
    /**
     * replaces possible nested bags with their elements, non recursively;
     * if <tt>this</tt> is a singleton bag of multiplicity one, the corresponding element;
     * @return an equivalent bag; <tt>this</tt> if it does no reduction 
     */
    @Override
    public BagfunctionTuple specSimplify() {
        if (Util.checkAny(support(), t -> t instanceof Bag<?>)) { //check for nested bags
            HashMap<BagfunctionTuple, Integer> copy = new HashMap<>(asMap());
            HashMap<TupleBag,Integer> nested = new HashMap<>();
            Iterator<Map.Entry<BagfunctionTuple, Integer>> ite = copy.entrySet().iterator();
            while (ite.hasNext()) {
                Map.Entry<BagfunctionTuple, Integer> e = ite.next();
                if ( e.getKey() instanceof Bag<?> ) {
                    nested.put((TupleBag) e.getKey(), e.getValue());
                    ite.remove(); //the nested bag is removed from copy
                }
            }
            //sum of nested         
            HashMap<BagfunctionTuple,Integer> sum = new HashMap<>();
            nested.entrySet().forEach(x -> { Bag.addAll(sum, x.getKey().asMap(), x.getValue()); });
            Bag.addAll(sum, copy, 1); //residual original bag elements are summed up
            
            return new TupleBag(sum);
        } 
      
        //return this;
      //NECESSARIO?
      Map.Entry<? extends BagfunctionTuple, Integer> e;
      return size() == 1 && (e = asMap().entrySet().iterator().next()) .getValue() == 1 ? e.getKey() : this;
    }

    @Override
    public  Class<BagfunctionTuple> bagType() {
        return BagfunctionTuple.class;
    }
   
}
