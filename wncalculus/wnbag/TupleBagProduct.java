package wncalculus.wnbag;

import java.util.*;
import wncalculus.tuple.AbstractTupleProduct;

/**
 *
 * @author lorenzo capra
 */
public final class TupleBagProduct extends AbstractTupleProduct<LinearComb,BagfunctionTuple> implements BagfunctionTuple {
    
    private TupleBagProduct (List<? extends BagfunctionTuple> tuples, boolean check)  {
        super(tuples, check);
    }
    
    /** 
     * main factory method; creates a product form a (non-empty) list of bag-function tuples
     * @param tuples a list of tuples
     * @param check check domain flag
     * @return the result of tuples juxtaposition
     * @throws IllegalDomain  if the tuple color domains are different;  many tuples of the same colour
     * or non mono-coloured tuples are present in the passed list
     */
    public static BagfunctionTuple factory (List<? extends BagfunctionTuple> tuples, boolean check)  {
        return tuples.size() == 1 ? tuples.get(0) : new TupleBagProduct (tuples, check); 
    } 
    
    /**
     * build a tuple juxtaposition without any check on tuples domains 
     * @param tuples a list of tuples to juxtapose
     * @return the tuples' juxtaposition
     * @throws IllegalDomain  if the tuples have different domains
     */
    public static BagfunctionTuple factory (List<? extends BagfunctionTuple> tuples)  {
        return factory(tuples, false);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * @param check_colors domain-check flag
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static BagfunctionTuple factory (boolean check_colors, BagfunctionTuple ... tuples) {
        return factory(Arrays.asList(tuples),  check_colors);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * without any check
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static BagfunctionTuple factory (BagfunctionTuple ... tuples) {
        return factory(false, tuples);
    }
    
    
    @Override
    public BagfunctionTuple buildOp(Collection<? extends BagfunctionTuple> args) {
        return factory((List<? extends BagfunctionTuple>) args, false);
    }
    
    @Override
    public BagfunctionTuple expand(int inner_sum) {
        TupleBag t_bag = (TupleBag) getArgs().get(inner_sum); //bag of tuples
        HashMap<BagfunctionTuple,Integer> prod_sum = new HashMap<>(); 
        t_bag.asMap().entrySet().forEach(e -> {
            List<BagfunctionTuple> simp_list = new ArrayList<>(getArgs());
            simp_list.set(inner_sum,  e.getKey());
            prod_sum.put(TupleBagProduct.factory(simp_list,false), e.getValue());
        });

        return new TupleBag(prod_sum);
    }
}
