package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.CharacterIterator;
import org.junit.Test;

import java.lang.*;
import java.lang.Character;
import java.util.Iterator;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayerTest {

    @Test
    public void layerTest(){

        LinguisticLayer layer = new LinguisticLayer();
        Iterator<Character> characters = new CharacterIterator("Daschöne");
        while(characters.hasNext()){
            char character = characters.next();
            LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(currentType);
            layer.feed(currentToken, 4);
        }
    }
}
