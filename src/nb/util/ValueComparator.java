package nb.util;
import java.util.Comparator;
import java.util.Map;

/**
 * Wrapper for sorting a Map<String,Double> by their values
 * @TODO Make this use generics.
 */
public class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } 
    }
}