package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.CharacterIterator;
import org.junit.Test;

import java.io.IOException;
import java.lang.*;
import java.lang.Character;
import java.util.Iterator;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayerTest {

    @Test
    public void layerTest() throws IOException {

        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache.txt");

        LinguisticLayer layer = new LinguisticLayer();
        Iterator<Character> characters = new CharacterIterator("abcd");
        //Iterator<Character> characters = corpus.tokens();
        int index = 0;
        int stepSize = 3;
        while(characters.hasNext()){
            char character = characters.next();
            LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(currentType);
            layer.feed(currentToken, 3);

            /*if(index % stepSize == stepSize -1)
                layer.processTrees(index-stepSize+1);
*/
            index++;
        }

        //layer.processTrees((index / stepSize) * stepSize);

        layer.processTrees();
        layer.checkTrees();
    }
}
