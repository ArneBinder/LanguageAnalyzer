package abinder.langanalyzer.helper;

import java.util.HashMap;

/**
 * Created by Arne on 22.09.2015.
 */
public class MultiSet<V> extends HashMap<V, Integer> {

    /*public MultiSet(){
        super();
    }*/

    public void add(V value){
        int count = 0;
        if(this.containsKey(value))
           count = get(value);
        put(value, ++count);
    }

}
