package abinder.langanalyzer.helper;

import java.util.HashMap;

/**
 * Created by Arne on 26.10.2015.
 */
public class ReconnectedMultiSet<V> extends MultiSet<V> {

    HashMap<V, V> keys = new HashMap<>();

    public ReconnectedMultiSet(int initialCapacity){
        super(initialCapacity);
    }

    public void add(V value){
        double count = 1;
        V key = keys.get(value);
        if(key!=null) {
            count += get(value);
            put(key, count);
        }else{
            put(value, count);
            keys.put(value, value);
        }
        totalCount++;

    }

    public void add(V value, double confidence){
        double count = confidence;
        V key = keys.get(value);
        if(key!=null) {
            count += get(value);
            put(key, count);
        }else{
            put(value, count);
            keys.put(value, value);
        }
        totalCount+= confidence;
    }

    public V getKey(V key){
        return keys.get(key);
    }
}
