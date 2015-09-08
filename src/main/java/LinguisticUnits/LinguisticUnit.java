package LinguisticUnits;

import java.util.ArrayList;

/**
 * Created by Arne on 08.09.2015.
 */
public abstract class LinguisticUnit {
    int fromIndex;
    int toIndex;
    int index;

    LinguisticLayer layer;

    LinguisticUnit(LinguisticLayer layer){
        this.layer = layer;
        index = layer.add(this);
    }

    // dummy
    LinguisticUnit(){
        System.out.println("ERROR: Empty constructur is not intended to use.");
    }

    ArrayList getContainingEntites(){
        if (isAtomic())
                return null;
        return layer.getLowerLayer().getUnits(fromIndex, toIndex);
    }

    ArrayList getLeftSiblings(int count){
        ArrayList result = new ArrayList();
        int fromIndex = index-count;
        while(fromIndex<0){
            fromIndex++;
            result.add(null);
        }
        result = layer.getLowerLayer().getUnits(fromIndex, index);

        return result;
    }

    ArrayList getRightSiblings(int count){
        ArrayList result = new ArrayList();
        ArrayList temp = new ArrayList();
        int toIndex = index+1+count;
        while(toIndex > layer.getSize()){
            toIndex--;
            temp.add(null);
        }
        result = layer.getLowerLayer().getUnits(index + 1, toIndex);
        result.addAll(temp);
        return result;
    }

    Boolean isAtomic(){
        return false;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public LinguisticLayer getLayer() {
        return layer;
    }

    public void setLayer(LinguisticLayer layer) {
        this.layer = layer;
    }


}
