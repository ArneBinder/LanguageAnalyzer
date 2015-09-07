package corpora.negra;

import LinguisticEntities.Sentence;
import LinguisticEntities.Token;
import corpora.Corpus;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 05.09.2015.
 */
public class NEGRACorpus implements Corpus {
    public void readFromFile(String filename) {

    }
    public void writeToFile(String filename){}

    public Corpus seperateEvalCorpus(double evalPercentage) {
        return null;
    }

    public Map<String, Object> getMetadata() {
        return null;
    }

    public Iterator<Token> token() {
        return null;
    }


}
