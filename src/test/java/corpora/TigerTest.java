package corpora;

import LinguisticEntities.Token;
import corpora.tiger.TIGERCorpus;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by Arne on 05.09.2015.
 */
public class TigerTest {

    @Test
    public void readCorpusTest() throws Exception{
        TIGERCorpus corpus = new TIGERCorpus();

        //Class clazz = this.getClass();
        //InputStream is = this.getClass().getClassLoader().getResourceAsStream("tiger_release_aug07.corrected.16012013_extract_v2.xml");
        //URL url = this.getClass().getClassLoader().getResource("tiger_release_aug07.corrected.16012013_extract_v2.xml");
        //System.out.println(clazz.getPackage().getClass().getClassLoader().getParent().getClass().getCanonicalName());
        corpus.readFromFile("src/test/resources/corpora/tiger/tiger_release_aug07.corrected.16012013_extract_v2.xml");
        Iterator<Token> token = corpus.token();
        while(token.hasNext()){
            System.out.print(token.next());
        }


        /*
        Corpus evalCorpus = corpus.seperateEvalCorpus(0.12);
        evalCorpus.writeToFile("eval_corpus.xml");
        corpus.writeToFile("train_corpus.xml");
        */
    }
}
