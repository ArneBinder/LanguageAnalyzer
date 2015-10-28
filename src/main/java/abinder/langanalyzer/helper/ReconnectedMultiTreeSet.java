package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;

/**
 * Created by Arne on 28.10.2015.
 */
public class ReconnectedMultiTreeSet extends ReconnectedMultiSet<LinguisticTree> {

    double leafTypeInfluence = 0.5;

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
}
