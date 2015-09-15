package abinder.langanalyzer.corpora;

import abinder.langanalyzer.LinguisticUnits.LinguisticModel;
import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.IO;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by Arne on 07.09.2015.
 */
public class WikipediaTest {

    @Test
    public void readCorpusTest() throws Exception {
        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();

        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache.txt");
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Syntax.txt");
        Iterator<java.lang.Character> tokens = corpus.tokens();
        String out = "";
        LinguisticModel model = new LinguisticModel();
        while(tokens.hasNext()){
            char character = tokens.next();
            model.feed(character);
            out += character;
            //System.out.print(tokens.next());
        }


        model.layer.printTypeNames();
        IO.writeFile("test.txt", out);
        System.out.println(out);
    }
}
