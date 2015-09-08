package LinguisticUnits;

import java.util.ArrayList;

/**
 * Created by Arne on 08.09.2015.
 */
public class LinguisticLayer<T extends LinguisticUnit> {

    private ArrayList<T> units = new ArrayList<T>();

    private LinguisticLayer higherLayer;  // more abstract
    private LinguisticLayer lowerLayer;   // closer to Tokens


    public ArrayList<T> getUnits() {
        return units;
    }

    public ArrayList<T> getUnits(int fromIndex, int toIndex) {
        return (ArrayList<T>) units.subList(fromIndex, toIndex);
    }

    public T getEntity(int index){
        return units.get(index);
    }

    public int getSize(){
        return units.size();
    }

    public int add(T unit){
        units.add(unit);
        return units.size()-1;
    }

    public void setUnits(ArrayList<T> units) {
        this.units = units;
    }

    public LinguisticLayer getHigherLayer() {
        return higherLayer;
    }

    public void setHigherLayer(LinguisticLayer higherLayer) {
        this.higherLayer = higherLayer;
    }

    public LinguisticLayer getLowerLayer() {
        return lowerLayer;
    }

    public void setLowerLayer(LinguisticLayer lowerLayer) {
        this.lowerLayer = lowerLayer;
    }


}
