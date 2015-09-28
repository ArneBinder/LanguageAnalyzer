package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;

/**
 * Created by Arne on 25.09.2015.
 */
public class MultiTreeSet extends MultiSet<LinguisticTree> {
    public void add(LinguisticTree value){
        int count = 0;
        if(this.containsKey(value))
            count = get(value);
        //else
        //    value.setParents(null);
        count++;
        totalCount++;
        put(value, count);
    }
}
