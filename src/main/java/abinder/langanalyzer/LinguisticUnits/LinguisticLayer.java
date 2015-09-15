package abinder.langanalyzer.LinguisticUnits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Arne on 08.09.2015.
 */
public class LinguisticLayer<T extends LinguisticUnit> {

    private ArrayList<T> tokens = new ArrayList<T>();
    private HashSet<T> knownTypes = new HashSet<T>();
    private HashMap<Integer, String> typeNames = new HashMap<Integer, String>();

    private LinguisticLayer higherLayer;  // more abstract
    private LinguisticLayer lowerLayer;   // closer to Characters


    public ArrayList<T> getTokens() {
        return tokens;
    }

    public ArrayList<T> getUnits(int fromIndex, int toIndex) {
        return (ArrayList<T>) tokens.subList(fromIndex, toIndex);
    }

    public T getEntity(int index){
        return tokens.get(index);
    }

    public int getLength(){
        return tokens.size();
    }

    public int add(T token, String name){
        typeNames.put(token.getType(), name);
        this.tokens.add(token);
        knownTypes.add(token);
        return this.tokens.size()-1;
    }

    @Override
    public String toString(){
        String result="";
        for(T currentToken: tokens){
            result += typeNames.get(currentToken.getType());
        }
        return result;
    }

    public void printTypeNames(){
        System.out.println("Type names for layer:");
        System.out.println(String.join(", ", typeNames.values()));
    }

    public LinguisticLayer aggregate(LinguisticLayer prevUpperLayer){

        return null;
    }

    public void setTokens(ArrayList<T> tokens) {
        this.tokens = tokens;
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
