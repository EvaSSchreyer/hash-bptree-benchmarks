package org.example;

import java.util.ArrayList;

/**
 * Counts T, every Element T maps to 1
 * @param <T>
 */

public class ConCatKeyMonoid<T extends Comparable<T>> implements Monoid<ConCatKeyMonoid<T>, T>{
    int count;//addition - this does the same as the CountingMonoid
    long hash;//xor - this is for the hash-computation (computeFingerprints); this stores the aggregated hash value
    ArrayList<T> concatKey;//the Key will be converted to a string and concatenated
    T greatestElement;//this saves the greatest Key (LeafNode Key) of the entire subtree of this Node

    public ConCatKeyMonoid(int count, long hash, ArrayList<T> concatKey, T greatestElement){
        this.count = count;
        this.hash = hash;
        this.concatKey = concatKey;
        this.greatestElement = greatestElement;
    }

    public ConCatKeyMonoid<T> identity() {
        return new ConCatKeyMonoid<>(0, 0, null,null);
    }

    public ConCatKeyMonoid<T> combine(ConCatKeyMonoid<T> x, ConCatKeyMonoid<T> y) {
        int compare;
        //null is our minimum element
        if (x.greatestElement == null){
            compare = -1;
        } else if (y.greatestElement == null) {
            compare = 1;
        } else {
            compare = x.greatestElement.compareTo(y.greatestElement);//1 if x > y, 0 if =, -1 if <
        }
        T tmp;
        if (compare > 0){
            tmp = x.greatestElement;
        } else {
            tmp = y.greatestElement;
        }
        ArrayList<T> concatTMP = new ArrayList<T>();
        if (x.concatKey != null){
            concatTMP.addAll(x.concatKey);
        }
        if (y.concatKey != null){
            concatTMP.addAll(y.concatKey);
        }
        return new ConCatKeyMonoid<>(x.count+y.count, x.hash^y.hash, concatTMP, tmp);
    }

    public ConCatKeyMonoid<T> mapIntoMonoid(T x) {
        ArrayList<T> concatTMP = new ArrayList<T>();
        concatTMP.add(x);
        return new ConCatKeyMonoid<>(1, x.hashCode(), concatTMP, x);
    }

    public int compare(ConCatKeyMonoid<T> x, T y){
        int compare;
        T tmpX = x.greatestElement;
        //null is our minimum element
        if (tmpX == null){
            compare = -1;
        } else if (y == null) {
            compare = 1;
        } else {
            compare = tmpX.compareTo(y);
        }
        return compare;//1 if x > y, 0 if =, -1 if <
    }
}