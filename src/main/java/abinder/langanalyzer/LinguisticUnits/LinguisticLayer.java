package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.IO;
import abinder.langanalyzer.LinguisticUnits.*;

import java.io.*;
import java.lang.*;
import java.lang.Character;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Arne on 08.09.2015.
 */
public class LinguisticLayer<T extends LinguisticUnit> {

    private ArrayList<T> tokens = new ArrayList<>();
    private HashSet<Integer> knownTypes = new HashSet<>();
    private HashMap<Integer, String> typeNames = new HashMap<>();

    private LinguisticLayer higherLayer;  // more abstract
    private LinguisticLayer lowerLayer;   // closer to Characters

    private static final char unitSeperator = '%';
    private static final char serializeSeperator = '\n';
    private static final char mapSeperator = ':';
    static Set<java.lang.Character> escapeableChars;

    static{
        java.lang.Character[] chars = {unitSeperator, serializeSeperator, mapSeperator};
        escapeableChars = new HashSet<>(Arrays.asList(chars));
    }

    private static final char escapeChar = '\\';

    private String contentClassName;

    public LinguisticLayer(String contentClassName){
        this.contentClassName = contentClassName;
    }

    public String getContentClassName() {
        return contentClassName;
    }

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

    public void add(T token, String name){
        typeNames.put(token.getType(), name);
        this.tokens.add(token);
        knownTypes.add(token.getType());
        //return this.tokens.size()-1;
    }

    public void add(T token){
        this.tokens.add(token);
        knownTypes.add(token.getType());
        //return this.tokens.size()-1;
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
        // TODO: implement (Clustering)!
        return null;
    }

    public void calcSequenceModel(){
        // TODO: implement!
    }

    public T predict(){
        // TODO: implement
        return null;
    }

    public HashMap getFeatures(){
        // TODO: implement!
        return null;
    }



    public void serialize(String filename) throws IOException{
        Writer out = new OutputStreamWriter(
                new FileOutputStream(filename+"."+contentClassName), "UTF-8");

        if(higherLayer!=null){
            out.write(higherLayer.getContentClassName());
            higherLayer.serialize(filename);
        }
        out.write(serializeSeperator);
        for(T token: tokens){
            out.write(token.serialize());
            out.write(unitSeperator+"");
        }
        out.write(serializeSeperator);
        for(Integer type: knownTypes){
            out.write(type+"");
            out.write(mapSeperator+"");
            out.write(escape(typeNames.get(type)));
            out.write(unitSeperator+"");
        }
        out.write(serializeSeperator);
        out.flush();
    }

    public void deserialize(String filename) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String contentClassName = filename.substring(filename.lastIndexOf(".")+1);
        System.out.println("contentClassName: "+contentClassName);

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
        final int blockSize = 4000;
        char buffer[] = new char[blockSize + 1];
        int count = in.read(buffer, 0, blockSize);
        int mode = 0;
        String temp = "";
        String higherLayerContentClassName = null;
        while(count != -1){
            for(int i = 0; i < count; i++) {
                char c = buffer[i];

                switch(mode){
                    case 0:
                        if (c == serializeSeperator){
                            higherLayerContentClassName = temp;
                            temp = "";
                            mode++;
                        }else{
                            temp+=c;
                        }
                        break;
                    case 1:
                        if (c == unitSeperator){
                            String thisPackage = this.getClass().getPackage().getName();
                            T unit = (T) IO.newInstance(thisPackage+"."+contentClassName, this, temp);
                            add(unit);
                            temp = "";
                        }else if(c==serializeSeperator) {
                            mode++;
                        }else{
                            temp+=c;
                        }
                        break;
                    case 2:
                        if (c == escapeChar){
                            mode=3;
                        }else if(c == unitSeperator){
                            String[] parts = temp.split(mapSeperator+"");
                            typeNames.put(Integer.parseInt(parts[0]), parts[0].substring(1)); //unescape

                            temp = "";
                        }else if(c==serializeSeperator) {
                            mode++;
                        }else{
                            temp+=c;
                        }
                        break;
                    case 3: // escape
                        temp += c;
                        mode = 2;
                        break;
                }
            }
            if(count < blockSize)
                break;
            count = in.read(buffer, 0, blockSize);
        }
        if(higherLayerContentClassName != null){
            higherLayer = new LinguisticLayer(higherLayerContentClassName);
            higherLayer.serialize(filename.substring(0, filename.lastIndexOf("."))+"."+higherLayerContentClassName);
        }
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


    private String escape(String str){
        String result = "";
        for(int i=0; i<str.length();i++){
            if(escapeableChars.contains(str.charAt(i)))
                result+=escapeChar;
            result+=str.charAt(i);
        }
        return result;
    }



}
