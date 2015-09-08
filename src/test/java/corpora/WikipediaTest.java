package corpora;

import LinguisticUnits.Token;
import corpora.wikipedia.WIKIPEDIACorpus;
import helper.IO;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by Arne on 07.09.2015.
 */
public class WikipediaTest {

    @Test
    public void readCorpusTest() throws Exception {
        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();

        corpus.readFromFile("src/test/resources/corpora/wikipedia/sprache.txt");
        corpus.readFromFile("src/test/resources/corpora/wikipedia/syntax.txt");
        Iterator<Token> tokens = corpus.tokens();
        String out = "";
        while(tokens.hasNext()){
            out += tokens.next();
            //System.out.print(tokens.next());
        }
        IO.writeFile("test.txt", out);
        System.out.println(out);
    }
}
