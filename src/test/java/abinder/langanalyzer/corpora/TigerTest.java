package abinder.langanalyzer.corpora;

import abinder.langanalyzer.corpora.tiger.TIGERCorpus;
import org.junit.Test;

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
        corpus.readFromFile("src/test/resources/abinder.langanalyzer.corpora/tiger/tiger_release_aug07.corrected.16012013_extract_v2.xml");
        Iterator<java.lang.Character> tokens = corpus.tokens();
        while(tokens.hasNext()){
            System.out.print(tokens.next());
        }
        System.out.println();
        //System.out.println("corpus.getBasicLayer().getLength(): "+corpus.getBasicLayer().getLength());
        // test, if other iterator (basicLayer) is used
        tokens = corpus.tokens();
        while(tokens.hasNext()){
            System.out.print(tokens.next());
        }
        System.out.println();
        //System.out.println("corpus.getBasicLayer().getLength(): "+corpus.getBasicLayer().getLength());

        /*
        Corpus evalCorpus = corpus.seperateEvalCorpus(0.12);
        evalCorpus.writeToFile("eval_corpus.xml");
        corpus.writeToFile("train_corpus.xml");
        */
    }
}
