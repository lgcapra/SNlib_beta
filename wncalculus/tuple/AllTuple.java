package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.All;
import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.Sort;
import wncalculus.util.ComplexKey;

/**
 * "singleton-like" class implementing universe-set tuples (that is, constant function-tuples
 mapping to U) of given size
 * @author lorenzo capra
 */
//def da rivedere: integrare con FunctionTuple.buildConstantTuple
public final class AllTuple extends ConstantTuple {

    /**
     * creates a map between colors and sub-lists of <tt>All</tt> functions
     * mathcing the specified codomain
     * @param codom the funtion's codomain
     * @return a map between colors and sub-lists of constants functions
     * matching the codomain
     */
    public static SortedMap<ColorClass, List<? extends SetFunction>> toMap(Domain codom) {
        TreeMap<ColorClass, List<? extends SetFunction>> map = new TreeMap<>();
        codom.support().forEach(s -> {
            ColorClass cc = (ColorClass) s;
            map.put(cc, Collections.nCopies(codom.mult(s), All.getInstance(cc)));
        });
        
        return map;
    }
    
    
    private AllTuple(Domain codom, Domain dom) {
        super(codom, dom);
    }
    
    private static final Map<ComplexKey, AllTuple> AT_VALUES = new HashMap<>();
    
    /**
     * main builder method
     * @param codom the function's codomain
     * @param dom the function's domain
     * @return a constant function matching the universe
     */
    public static AllTuple getInstance(Domain codom, Domain dom) {
        ComplexKey k = new ComplexKey(codom, dom);
        AllTuple at = AT_VALUES.get(k);
        if ( at == null) {
            AT_VALUES.put(k, at = new AllTuple( codom, dom));
            at.setSimplified(true); //OPTIMIZATION
        }
        
        return at;
    }
    
    @Override
    public boolean isTrue() {
        return true;
    }
    
    @Override
    public AllTuple clone(Domain newdom, Domain newcd) {
        return getInstance(newcd, newdom);
    }
    

    @Override
    public boolean differentFromZero() {
        return true;
    }

    /**
     * 
     * @return the corresponding <code>Tuple</code> object 
     */
    @Override
    public Tuple asTuple() {
        Domain cd = getCodomain();
        Tuple res = new Tuple( null, cd, toMap(cd), null, getDomain()) ;
        res.setSimplified(true);
        
        return res;
    }
    
    @Override
    public boolean isTuple() {
        return true;
    }
    
    @Override
    public Integer cardLb() {
        int card = 1;
        for (Map.Entry<? extends Sort, Integer> x : getCodomain().asMap().entrySet()) {
            int lb = x.getKey().lb();
            for (int i = 0; i < x.getValue() ; i++)
                card = Math.multiplyExact(card , lb);
        }
        
        return card;
    }
    

    @Override
    String symbol() {
        return "All";
    }
    
    
    @Override
    public String toStringAbstract() {
        return asTuple().toStringAbstract();
    }


}
