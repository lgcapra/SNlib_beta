package wncalculus.tuple;

import java.util.*;
import wncalculus.logexpr.AndOp;
import wncalculus.classfunction.Intersection;
import wncalculus.expr.IllegalDomain;
import wncalculus.guard.And;
import wncalculus.guard.Guard;
import wncalculus.util.Util;
import wncalculus.classfunction.SetFunction;
import wncalculus.expr.MultiArgs;

/**
 * this class defines the intersection of WN function-tuples  mapping on sets
 * @author Lorenzo Capra
 */
public final class TupleIntersection extends TupleNaryOp implements AndOp<FunctionTuple > {
    
    private TupleIntersection (Set<?  extends FunctionTuple> guards, boolean check) {
        super(guards, /*check*/true);
    }
    
    
    /** builds a TupleIntersection from a given collection of operands;
     * if the collection is a singleton then the contained element is returned
     * the size of tuples is checked if the corresponding flag is set
     * @param arglist the collection of operands
     * @param check the arity control flag
     * @return either a TupleIntersection corresponding to the passed collection, or the
     * contained element if the collection is size-one
     * @throws IllegalDomain if the operands have different arity
     * @throws NoSuchElementException if the collection is empty
     */
    public static FunctionTuple factory(Collection<? extends FunctionTuple> arglist, boolean check) {
        Set<? extends FunctionTuple> aSet = Util.asSet(arglist);
        return aSet.size() < 2 ?  aSet.iterator().next() : new TupleIntersection (aSet, check);
    }
    
    /**
     * builds a TupleIntersection from a given collection of operands, without any check
     * on their arity
     * @param arglist  the collection of operands
     * @return either a TupleIntersection corresponding to the passed collection, or the
     * only contained element, if the collection is size-one
     */
    public static FunctionTuple factory(Collection<? extends FunctionTuple> arglist)  {
        return factory(arglist,true);
    }
    
    /**
     * builds a TupleIntersection from a given list (varargs) of operands
     * @param check the arity control flag
     * @param args the list (varargs) of operands
     * @return either a TupleIntersection corresponding to the passed list, or the
     * only contained element, if the list is size-one
     */
    public static FunctionTuple factory (boolean check, FunctionTuple ... args)  {
        return factory(Arrays.asList(args), check); 
    }
    
    /**
     * builds a TupleIntersection from a given list (varargs) of operands
     * without any arity-check
     * @param args the list (varargs) of operands
     * @return either a TupleIntersection corresponding to the passed list, or the
     * only contained element, if the list is size-one
     */
    public static FunctionTuple factory(FunctionTuple ... args) {
        return factory(true, args);
    }
    
    @Override
    public FunctionTuple buildOp(Collection <? extends FunctionTuple > args)  {
        return factory(args, false);
    }

    @Override
    public boolean differentFromZero() {
        return getArgs().stream().allMatch( f -> f.differentFromZero() );
    }
        
    @Override
    public String symb() {
        return " * ";
    }

    /** translates a tuple intersection to a (possibly guarded) equivalent tuple
     * with inner intersections
     * @return an equivalent elementary tuple with inner intersections, if all operands are tuples;
     * otherwise, an intersection in which all tuple operands have been merged. 
     */
    @Override
    public FunctionTuple specSimplify() {
        FunctionTuple res = this;
        Map<Boolean, HashSet<FunctionTuple>> tmap = Util.mapFeature(getArgs(), t -> t instanceof Tuple);
        HashSet<FunctionTuple> tm = tmap.get(true);
        if ( tm != null && tm. size() > 1 ) { // more than one Tuple
            Collection<Tuple> tuples = Util.cast(tm, Tuple.class);
            Set<Guard> newguards  = new HashSet<>() , newfilters  = new HashSet<>();
            for (Tuple t : tuples ) {
               newguards.add(t.guard());
               newfilters.add(t.filter());
            }
            List<SetFunction> newcomponents = new ArrayList<>();
            for (int i = 0, tsize = size() ; i < tsize ; i++) { // the list of tuple-components made by inner intersections is built
                HashSet<SetFunction> comp_i = new HashSet<>();
                for (Tuple t : tuples) {
                    comp_i.add(t.getComponents().get(i));
                }
                try {
                    newcomponents.add( Intersection.factory(comp_i) ); 
                }
                catch (Exception e) {
                    System.out.println(tuples);
                    throw e;
                } 
            }     
            res = new Tuple(And.buildAndForm(newfilters), getCodomain(), newcomponents, And.buildAndForm(newguards), getDomain()); // the tuples intersection...
            Set<FunctionTuple> others = tmap.get(false);
            if (others != null) {//not all operands are tuples
                others.add(res);
                res = new TupleIntersection(others, false);
            }
        }
        //System.out.println(res); // debug  
        return res;
    }
    
    
    /**
     * method overriding: an intersection of tuples can never be considered a normal-and-form
     * @return <code>false</code> 
     */
    @Override
    public boolean isNormalAndForm() {
        return false;
    }
    
    @Override
    public Set<Class<? extends MultiArgs >> distributiveOps () {
        return Collections.singleton(TupleSum.class);
    }

}
