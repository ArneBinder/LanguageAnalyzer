package abinder.langanalyzer.helper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Arne on 22.09.2015.
 */
public class MultiSet<V> extends HashMap<V, Integer> implements Iterable<V> {

    int totalCount = 0;
    //HashMap<V,V> keyBackups = new HashMap<>();

    public int getTotalCount() {
        return totalCount;
    }

    public void add(V value){
        int count = 0;
        if(this.containsKey(value))
           count = get(value);
        count++;
        totalCount++;
        put(value, count);
        //keyBackups.put(value, value);
    }

    public double getProbability(V value){
        if(!this.containsKey(value))
            return 0;
        return get(value) / (double) totalCount;
    }

    /*public V getKey(V key){
        return keyBackups.get(key);
    }*/

    /*
    public void resetKey(V key){
        put(key, get(key));
    }
    */

    @Override
    public Iterator<V> iterator() {
        return keySet().iterator();
    }

    public Map<V, java.lang.Integer> sortByValue()
    {
        Map<V, java.lang.Integer> result = new LinkedHashMap<>();
        Stream<Entry<V, java.lang.Integer>> st = this.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    public SortedSet<V> sortedKeySet(){
       return new TreeSet<V>(this.keySet());
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream <Entry<K,V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }
}
