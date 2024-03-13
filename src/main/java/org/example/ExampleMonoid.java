package org.example;

/**
 * Counts T, every Element T maps to 1
 * This is a monoid that counts elements with the count variable, saves the hash
 * @param <T>
 */

public class ExampleMonoid<T extends Comparable<T>> implements Monoid<ExampleMonoid<T>, T>{
    int count;//addition - this does the same as the CountingMonoid
    long hash;//xor - this stores the aggregated hash value
    T greatestElement;//this saves the greatest Key contained in the subtree of a node or subrange

    public ExampleMonoid(int count, long hash, T greatestElement){
        this.count = count;
        this.hash = hash;
        this.greatestElement = greatestElement;
    }

    public ExampleMonoid<T> identity() {
        return new ExampleMonoid<>(0, 0, null);
    }

    /**
     * this function combines two monoids
     * @param x: first Monoid
     * @param y: second Monoid
     * @return a monoid whose values are the combination of the given two monoids
     */
    public ExampleMonoid<T> combine(ExampleMonoid<T> x, ExampleMonoid<T> y) {
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
        return new ExampleMonoid<>(x.count+y.count, x.hash^y.hash, tmp);
    }

    public ExampleMonoid<T> mapIntoMonoid(T x) {
        return new ExampleMonoid<>(1, x.hashCode(), x);
    }

    /**
     * Compares the greatestElement of a Monoid with a T element, i.e. a Key
     * @param x
     * @param y
     * @return 1 if x > y, 0 if x = y, -1 if x < y
     */
    public int compare(ExampleMonoid<T> x, T y){
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