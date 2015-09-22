package abinder.langanalyzer.helper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Arne on 22.09.2015.
 */
public class MultiSet<V> extends HashMap<V, Integer> implements Iterable<V> {

    public void add(V value){
        int count = 0;
        if(this.containsKey(value))
           count = get(value);
        count++;
        put(value, count);
    }

    @Override
    public Iterator<V> iterator() {
        return keySet().iterator();
    }

    public Map<V, java.lang.Integer> sortByValue()
    {
        Map<V, java.lang.Integer> result = new LinkedHashMap<>();
        Stream<Entry<V, java.lang.Integer>> st = this.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }

    /*public SortedSet<V> sortedKeySet(){
       return new TreeSet<V>(this.keySet());
    }*/
}
