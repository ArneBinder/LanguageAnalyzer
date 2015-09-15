package abinder.langanalyzer.LinguisticUnits;

import java.util.ArrayList;

/**
 * Created by Arne on 08.09.2015.
 */
public abstract class LinguisticUnit {
    int fromIndex;
    int toIndex;
    int index;
    int type;

    LinguisticLayer layer;

    LinguisticUnit(LinguisticLayer layer, int type, String name) {
        this.type = type;
        this.layer = layer;
        index = layer.add(this, name);
    }

    // dummy
    LinguisticUnit() {
        System.out.println("ERROR: Empty constructur is not intended to use.");
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if (type == 0)
            throw new NullPointerException("Type can not be null (0)!");
        this.type = type;
    }

    ArrayList getContainingEntites() {
        if (isAtomic())
            return null;
        return layer.getLowerLayer().getUnits(fromIndex, toIndex);
    }

    ArrayList getLeftSiblings(int count) {
        ArrayList result = new ArrayList();
        int fromIndex = index - count;
        while (fromIndex < 0) {
            fromIndex++;
            result.add(null);
        }
        result = layer.getLowerLayer().getUnits(fromIndex, index);

        return result;
    }

    ArrayList getRightSiblings(int count) {
        ArrayList result = new ArrayList();
        ArrayList temp = new ArrayList();
        int toIndex = index + 1 + count;
        while (toIndex > layer.getLength()) {
            toIndex--;
            temp.add(null);
        }
        result = layer.getLowerLayer().getUnits(index + 1, toIndex);
        result.addAll(temp);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LinguisticUnit))
            return false;
        if (obj == this)
            return true;
        LinguisticUnit rhs = (LinguisticUnit) obj;
        return type==rhs.getType();
    }


    @Override
    public int hashCode() {
        return type;
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
