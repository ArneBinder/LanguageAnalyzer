package abinder.langanalyzer.helper;

/**
 * Created by Arne on 16.10.2015.
 */
public class KeyValuePair<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<KeyValuePair<K, V>>{

    public K key;
    public V value;

    public KeyValuePair(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }



    @Override
    public int compareTo(KeyValuePair<K, V> o) {
        if(o==null)
            return 1;
        //return key==o.key?value.compareTo(o.value):(int)Math.signum(key-o.key);
        int comp = key.compareTo(o.key);
        return (comp==0)?value.compareTo(o.value):comp;
    }
}
