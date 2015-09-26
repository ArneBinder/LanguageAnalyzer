package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.CharacterIterator;
import org.junit.Test;

import java.io.*;
import java.lang.*;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayerTest {

    ArrayList<Long> timeStamps = new ArrayList<>();

    public LinguisticLayerTest(){
        timeStamps.add(System.currentTimeMillis());
    }

    private void printTimeMessage(String message){
        if(timeStamps.size()==0) {
            System.out.println("WARNING: timeStamps is empty");
            return;
        }
        long currTime = System.currentTimeMillis();
        System.out.println(message+": "+(currTime- timeStamps.get(timeStamps.size()-1))+" ms");
        timeStamps.add(currTime);
    }

    @Test
    public void layerTest() throws IOException {

        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache.txt");
        //corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Syntax.txt");
        printTimeMessage("corpus read");

        LinguisticLayer layer = new LinguisticLayer();
        //Iterator<Character> characters = new CharacterIterator("abcd");
        Iterator<Character> characters = corpus.tokens();
        int index = 0;
        int stepSize = 3;
        while(characters.hasNext()){
            char character = characters.next();
            LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(currentType);
            layer.feed(currentToken, 3);

            /*if(index % stepSize == stepSize -1)
                layer.updateTreePatterns(index-stepSize+1);
*/
            index++;
        }

        printTimeMessage("fed content");
        //layer.updateTreePatterns((index / stepSize) * stepSize);

        layer.updateTreePatterns();
        printTimeMessage("updateTreePatterns");

        layer.calculateTreePatternProbabilites();
        printTimeMessage("calculateTreePatternProbabilities");


        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("out.txt")));
        layer.printProbabilitiesSortedByValue(out);
        out.flush();
        printTimeMessage("printProbabilitiesSortedByValue to file");

    }
}
