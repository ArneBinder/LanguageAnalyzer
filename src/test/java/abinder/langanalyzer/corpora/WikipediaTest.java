package abinder.langanalyzer.corpora;

import abinder.langanalyzer.LinguisticUnits.LinguisticModel;
import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.IO;
import com.google.gson.Gson;
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


        //Gson gson = new Gson();
        model.layer.printTypeNames();
        model.layer.serialize("layer");
        LinguisticModel model2 = new LinguisticModel();
        model2.layer.deserialize("layer.Character");
        model2.layer.printTypeNames();
        IO.writeFile("test.txt", out);
        System.out.println(out);
        //System.out.println(gson.toJson(model.layer.getTokens()));
    }
}
