package wncalculus.expr;

import java.util.*;
import wncalculus.util.Util;

//SI POTREBBE DEFINIRE COME GENERICA RISPETTO AL TIPO DI SORTE

/**
 * this class defines a domain (i.e., Cartesian product) of sorts;
 * even if permitted, a domain shouldn't hold different sorts with the same name,
 * for the sake of consistency
 * @author Lorenzo Capra
 */
public final class Domain {
    
    private final Map<Sort,Integer > domain; 
    
    private String string; //cashing
      
    /** 
     * creates a domain from a (non-empty) map of sorts to integers (i.e., a multiset of sorts)
     * note that, since an hash-map is used, different sorts should have different names
     * to avoid any confusion/incongruence (no check is done aboout that)
     * @param sm an hash-map of sorts to integers
     */
    public Domain(HashMap<? extends Sort,Integer> sm) {
        if (sm.isEmpty())
            throw new IllegalDomain("you cannot build an empty domain!");
        
        this.domain = Collections.unmodifiableMap(sm); //this cast is just syntactical
    }
    
    /**
     * build a single-sort domain
     * @param s the domain's sort
     * @param k the sort mult
     */
    public Domain(Sort s, int k) {
        this(Util.singleMap(s,k));
    }
    
    /** 
     * creates a domain from a list of sorts
     * @param sortlist a list of sorts
     */
    public Domain(List<? extends Sort> sortlist)  {
        this (Util.asMap(sortlist));
    }
    
    /**
     * creates a domain from an arbitrary number of sorts
     * @param args a var-args of sorts
     */
    public Domain (Sort ... args) {
        this(Arrays.asList(args));
    }
                
    /**
     * @return the domain's support, i.e., the set of sorts composing the domain
     * (actually a @see {SortedSet}) 
     */
    public Set<Sort> support () {
        return  this.domain.keySet();
    }
    
    /**
     * 
     * @return the domain's size, i.e., the size of its support  
     */
    public int size() {
        return this.domain.size();
    }
    
    /**
     * @param sort a sort
     * @return the multiplicity of the sort; 0 if the sort is not mapped
     */
    public int mult (Sort sort) {
        return this.domain.getOrDefault(sort, 0);
    }
    
    /**
     * 
     * @param sortname a sort's name
     * @return the corresponding sort; <tt>null</tt> if no sort with thhat name is mapped
     */
    public Sort sort (String sortname) {
        for ( Sort s : this.domain.keySet() ) 
            if (s.name().equals(sortname))
                return s;
        
        return null;
    }
      
    /**
     * sets up a multiplicity for a specified sort in a NON destructive way
     * if there is a sort with the same name of the passed one (possibly different)
     * then the existing mapping is overwritten
     * @param s a sort
     * @param mult a multiplicity value
     * @return a new domain obtained from <code>this</code> by setting up the given
     * multiplicity for the givev sort; <code>this</code> if the sort is currently
     * assigned the specified multiplicity
     */ 
    public Domain set(Sort s, int mult) {
         HashMap<Sort, Integer> copy = new HashMap<>(this.domain);
         copy.put(s, mult);
         
         return new Domain(copy);
    }
       
    /**
     * replaces <tt>this</tt> domain's sorts with those specified by the map, if any  
     * @param sort_map the map between old and new sorts
     * @return the domain resulting from the merge of <tt>this</tt> domain with the given new sorts;
     * <tt>this</tt> if the specified map is empty
     */
    public Domain setSupport (Map<? extends Sort, ? extends Sort> sort_map) {
        if (sort_map.isEmpty())
            return this;
        else {
            HashMap<Sort,Integer > copy = new HashMap<>();
            this.domain.entrySet().forEach (e -> {
                Sort k = e.getKey(), v = sort_map.get(k); //old and new split sort
                copy.put(v != null ? v : k, e.getValue());
            });

            return new Domain(copy);
        }
    }
    
    /*public Domain setSupport2 (Set<? extends Sort> newsorts) {
        if (newsorts.isEmpty())
            return this;
        else {
            HashMap<Sort,Integer > copy = new HashMap<>();
            this.domain.entrySet().forEach (e -> {
                Sort k = e.getKey(), v = newsorts.contains(k) ? k :  sort(); //old and new split sort
                copy.put(v != null ? v : k, e.getValue());
            });

            return new Domain(copy);
        }
    }*/
    
    @Override
    public boolean equals (Object o) {
        return this == o || o instanceof Domain && ((Domain)o).domain.equals(this.domain);
    }

    @Override
    public int hashCode() {
        return 59 + Objects.hashCode(this.domain);
    }
    
   
    /** 
     * @return a map-view of this domain 
     */
    public Map<? extends Sort,Integer> asMap() {
        return this.domain;
    }
            
    @Override
    public String toString() {
       if (this.string == null)
           this.string = new TreeMap<>(this.domain).toString();
       
       return this.string;
    }
    
    /**
     * 
     * @return the list of (possibly repeated) names of sorts of the domain 
     */
    public StringBuilder names() {
        StringBuilder s = new StringBuilder("@");
        new TreeMap<>(this.domain).entrySet().forEach( x -> { s.append(x.getKey().name()).append('^').append(x.getValue()).append(','); });
        s.deleteCharAt(s.length()-1);
        
        return s;
    }
            
}