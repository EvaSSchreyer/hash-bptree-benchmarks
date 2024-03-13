package org.example;

/**
 * A {@link Monoid} is the pairing of an associative function with an identity element.
 * @param <M> the element type this Monoid is formed over
 */
public interface Monoid<M, U>{
    /**
     * The identity element of this monoid.
     * @return the identity
     */
    M identity();

    /**
     * Combines two Monoid Elements (IMPORTANT: the function that combines the monoids has to be associative)
     * @param x: first Monoid
     * @param y: second Monoid
     * @return: the new Monoid (that is a combination of x and y)
     */
    M combine(M x, M y);

    /**
     * This maps an Element of Type U (e.g. the Key of a Node) into a Monoid
     * @param x: element that shall e mapped into a Monoid
     * @return: a Monoid
     */
    M mapIntoMonoid(U x);

    int compare(M x, U y);
}