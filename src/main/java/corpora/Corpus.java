package corpora;

import LinguisticUnits.LinguisticLayer;
import LinguisticUnits.Token;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Arne on 05.09.2015.
 */
public abstract class Corpus {
    LinguisticLayer basicLayer = new LinguisticLayer<Token>();

    public LinguisticLayer getBasicLayer() {
        return basicLayer;
    }

    abstract public void readFromFile(String filename) throws IOException;

    abstract public void writeToFile(String filename);

    abstract public Corpus seperateEvalCorpus(double evalPercentage) throws Exception;

    abstract public Map<String, Object> getMetadata();

    abstract public Iterator<Token> tokens();



}
