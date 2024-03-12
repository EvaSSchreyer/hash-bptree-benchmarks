/**
 * Counts T, every Element T maps to 1
 * @param <T>
 */
public class CountingMonoid<T> implements Monoid<Integer, T>{

    public Integer identity() {
        return 0;
    }

    public Integer combine(Integer x, Integer y) {
        return x+y;
    }

    public Integer mapIntoMonoid(T x) {
        return 1;
    }

    @Override
    public int compare(Integer x, T y) {
        return 0;
    }
}
