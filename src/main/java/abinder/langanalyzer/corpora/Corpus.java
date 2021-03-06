package abinder.langanalyzer.corpora;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Arne on 05.09.2015.
 */
public abstract class Corpus {
    /*LinguisticLayer_dep basicLayer = new LinguisticLayer_dep<Character>();

    public LinguisticLayer_dep getBasicLayer() {
        return basicLayer;
    }*/
    private int size = 0;

    public int getSize() {
        return size;
    }

    protected void incSize(int value){
        size += value;
    }

    abstract public void readFromFile(String filename) throws IOException;

    abstract public void writeToFile(String filename);

    abstract public Corpus seperateEvalCorpus(double evalPercentage) throws Exception;

    abstract public Map<String, Object> getMetadata();

    abstract public Iterator<java.lang.Character> tokens();



}
