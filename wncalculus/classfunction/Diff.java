package wncalculus.classfunction;

import wncalculus.expr.*;

/**
 * this marker class implements the difference between class-functions mapping to sets
 * @author lorenzo capra
 */
public final class Diff   {

    /**
     * factory method: builds a <tt>SetFuncton</tt> which is equivalent to the difference between the operands,
     * @param min the minuend
     * @param subtr the subtrahend
     * @param check domain-check flag
     * @return the <tt>SetFunction</tt> a function equivalent to the difference
     * @throws IllegalDomain if the arity of operands is not the same
     */
    public static SetFunction factory (SetFunction min , SetFunction subtr , boolean check) { // anticipates specific reductions
       if (check && !min.getSort().equals(subtr.getSort())) 
            throw new IllegalDomain(min+" ("+min.getSort()+") "+subtr+" ("+subtr.getSort()+")");
        
        if (min.isFalse() || subtr.isTrue()) 
            return min.getFalse(); // might be a general rule 
        
        if ( subtr.isFalse()) 
            return min; //as above
         
        if (min.isTrue())
            return Complement.factory(subtr);  

        return Intersection.factory(min , Complement.factory(subtr)) ;
    }
    
     /** overloaded constructor not checking color' compliance
     * @param min the minuend
     * @param subtr the subtrahend
     * @return the <tt>SetFunction</tt> which is the difference between the operands
     */
    public static SetFunction factory (SetFunction min , SetFunction subtr )  {
        return factory (min, subtr, false);
    }
}
