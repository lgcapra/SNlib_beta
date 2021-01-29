package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.classfunction.SetFunction;

/**
 * This class defines a kind of tuple juxtaposition operator.
 * Tuple-arguments must all have the same domain, single-coloured and pairwise-disjoint codomains.
 * @author Lorenzo Capra
 */
public final class TupleProduct extends AbstractTupleProduct<SetFunction,FunctionTuple> implements FunctionTuple   {
    
    /*
    base constructor
    */
    private TupleProduct (List<? extends FunctionTuple> tuples, boolean check)  {
        super(tuples, check);
    }
    
    /** 
     * main factory method; creates juxtaposition form a (non-empty) list of Tuples
     * @param tuples a list of tuples
     * @param check check domain flag
     * @return the result of tuples juxtaposition
     * @throws IllegalDomain  if the tuple color domains are different;  many tuples of the same colour
     * or non mono-coloured tuples are present in the passed list
     */
    public static FunctionTuple factory (List<? extends FunctionTuple> tuples, boolean check)  {
        return tuples.size() == 1 ? tuples.get(0) : new TupleProduct (tuples, check); 
    } 
    
    /**
     * build a tuple juxtaposition without any check on tuples domains 
     * @param tuples a list of tuples to juxtapose
     * @return the tuples' juxtaposition
     * @throws IllegalDomain  if the tuples have different domains
     */
    public static FunctionTuple factory (List<? extends FunctionTuple> tuples)  {
        return factory(tuples, false);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * @param check_colors domain-check flag
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static FunctionTuple factory (boolean check_colors, FunctionTuple ... tuples) {
        return factory(Arrays.asList(tuples),  check_colors);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * without any check
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static FunctionTuple factory (FunctionTuple ... tuples) {
        return factory(false, tuples);
    }
    
    @Override
    public FunctionTuple buildOp(Collection<? extends FunctionTuple > args) {
        return factory((List<? extends FunctionTuple>) args, false);
    }
    
    @Override
    public boolean differentFromZero() {
        return getArgs().stream().allMatch(t -> t.differentFromZero());
    }

    //@Override
    /** the simplification algorithm treats two cases; a juxtaposition formed only by 
        Tuples; a juxtaposition containing a n-ary operator*/
    /*@Override
    public FunctionTuple specSimplify() { 
       List<FunctionTuple> tuples = getArgs();
       //if ( Util.find(tuples, EmptyTuple.class ) != null)
          //return  nullExpr().cast(); //optimization
       
       if (Util.checkAny(tuples, p -> p.isNull()))
           return nullExpr().cast(); 
       
       if (Util.checkAll(tuples , (e -> e.isTuple()) ) ) { // the are only (constant) Tuples (non extended) ..
           Domain cd = getCodomain(), d = getDomain();
           List<SetFunction> t_list = new ArrayList<>();
           Set<Guard> g = new HashSet<>(), f = new HashSet<>();
           for (FunctionTuple x : tuples ) {
               AbstractTuple<SetFunction,FunctionTuple> t = x.asTuple();
               t_list.addAll( t.getComponents());
               if (t.filter() != null)  //optimization
                   f.add(t.filter().clone(cd));  // the domains of the inner filters are different from the terms's codomain...
               if (t.guard() != null) 
                   g.add(t.guard());
           }
           return new Tuple(And.buildAndForm(f), cd, t_list, And.buildAndForm(g, d), d);
       }
       
       return expand( Util.indexOf(tuples, p -> p.isElementarySum()) );
    }*/

    @Override
    public FunctionTuple expand(int inner_sum) {
        TupleSum t_op = (TupleSum) getArgs().get(inner_sum);
        Set<FunctionTuple> t_prod = new HashSet<>(); 
        for (FunctionTuple x : t_op.getArgs() ) {
            List<FunctionTuple> simp_list = new ArrayList<>(getArgs());
            simp_list.set(inner_sum, x );
            t_prod.add(TupleProduct.factory(simp_list,false));
        }

        return TupleSum.factory(t_prod, t_op.disjoined());
    }
    
}
