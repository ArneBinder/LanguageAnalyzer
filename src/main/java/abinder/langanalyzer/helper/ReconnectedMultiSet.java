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
        keys.put(value, value);
        super.add(value);
    }

    public void add(V value, double confidence){
        keys.put(value, value);
        super.add(value, confidence);
    }

    public V getKey(V key){
        return keys.get(key);
    }
}
