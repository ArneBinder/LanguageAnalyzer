package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Arne on 28.10.2015.
 */
public class ReconnectedMultiTreeSet extends ReconnectedMultiSet<LinguisticTree> {

    private double leafTypeInfluence = 0.5;
    private double maxSize = 1000;


    public ReconnectedMultiTreeSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public double getRelFrequ(LinguisticTree value){
        if(value.isLeaf()) {
            double result = 0;
            if(this.containsKey(value))
                result += get(value) * (1.0-leafTypeInfluence) / totalCount;
            LinguisticTree typedTree = new LinguisticTree(value.getLabel());
            if(this.containsKey(typedTree))
                result += get(typedTree) * leafTypeInfluence / totalCount;
            return result;
        }else{
            if(!this.containsKey(value)) {
                return 0;
            }
            return get(value) / totalCount;
        }
    }

    @Override
    public void merge(LinguisticTree oldKey, LinguisticTree newKey){
        if(newKey.getPartitions()!=null)
            oldKey.setPartitions(newKey.getPartitions());
        if(newKey.getProbability()!=0)
            oldKey.setProbability(newKey.getProbability());
    }

    public void clean(){
        while(totalCount > maxSize + 1){ // add 1, otherwise it could hang
            double rem = (totalCount-maxSize) / size();
            for(Iterator<Entry<LinguisticTree, Double>> it = entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<LinguisticTree, Double> entry = it.next();
                double newValue = entry.getValue() - rem;
                if(newValue > 0) {
                    entry.setValue(newValue);
                    totalCount -= rem;
                }else{
                    totalCount -= entry.getValue();
                    keys.remove(entry.getKey());
                    it.remove();
                }
            }
        }
    }

}
