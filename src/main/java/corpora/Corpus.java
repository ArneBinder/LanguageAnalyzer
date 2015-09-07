package corpora;

import LinguisticEntities.Sentence;
import LinguisticEntities.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 05.09.2015.
 */
public interface Corpus {
    void readFromFile(String filename);

    void writeToFile(String filename);

    Corpus seperateEvalCorpus(double evalPercentage) throws Exception;

    Map<String, Object> getMetadata();

    Iterator<Token> token();



}
