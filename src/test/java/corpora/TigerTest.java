package corpora;

import LinguisticEntities.Token;
import corpora.tiger.TIGERCorpus;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by Arne on 05.09.2015.
 */
public class TigerTest {

    @Test
    public void readCorpusTest() throws Exception{
        TIGERCorpus corpus = new TIGERCorpus();

        corpus.readFromFile("tiger_release_aug07.corrected.16012013_extract_v2.xml");
        Iterator<Token> characters = corpus.token();
        while(characters.hasNext()){
            System.out.print(characters.next());
        }
        /*
        Corpus evalCorpus = corpus.seperateEvalCorpus(0.12);
        evalCorpus.writeToFile("eval_corpus.xml");
        corpus.writeToFile("train_corpus.xml");
        */
    }
}
