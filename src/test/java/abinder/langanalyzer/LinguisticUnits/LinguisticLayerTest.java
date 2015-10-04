package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.CharacterIterator;
import abinder.langanalyzer.helper.Sum;
import org.junit.Test;

import java.io.*;
import java.lang.*;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

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
    public void deserializationTest(){
        LinguisticTree newTree = new LinguisticTree("[\\X,[[e,[a,k]],[m,e]]]");
        //System.out.println(newTree.serialize(false));
        assertEquals("[\\X,[[e,[a,k]],[m,e]]]", newTree.serialize(false));
    }

    @Test
    public void simpleLayerTest(){
        LinguisticLayer layer = new LinguisticLayer();
        LinguisticTree tree = new LinguisticTree("[[a,b],[c,d]]");
        for(LinguisticTree subTree: tree.getAllSubtrees(3)){
            layer.addAllTreePattern(subTree.getAllCutTrees());
        }
        //ArrayList<String> str = new ArrayList<>();
        System.out.println(layer.getProb(tree));
        /*for(String s:layer.getPartitions(tree)){
            System.out.println(s);
        }*/
        Sum sum = layer.getOperations(tree);
        //System.out.println(sum.calculate(layer.getTreePatterns()));

        sum.deepFlatten();
        //System.out.println(sum);
        //String partitionString = sum.toString();
        //String[] partitions = partitionString.substring(1, partitionString.length()-1).split(" \\+ ");
        //Arrays.sort(partitions);
        //System.out.println("\npartitions:\n"+String.join("\n", Arrays.asList(partitions))+"\n");
        System.out.println("\npartitions:\n"+sum.toString().replaceAll(" \\+ ", "\n")+"\n");

        System.out.println("probability:\t"+ sum.calculate(layer.getTreePatterns()));
        System.out.println("Done");
    }

    @Test
    public void layerTest() throws IOException {

        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache.txt");
        //corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Syntax.txt");
        printTimeMessage("corpus read");

        PrintStream outc = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outc.txt"))), true, "UTF-8");
        LinguisticLayer layer = new LinguisticLayer();
        //Iterator<Character> characters = new CharacterIterator("abcd");
        Iterator<Character> characters = corpus.tokens();
        int index = 0;
        int stepSize = 3;
        System.out.println();
        while(characters.hasNext()){
            char character = characters.next();
            LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(currentType);
            if(!currentToken.serialize(false).equals("\\n"))
                System.out.print(currentToken.serialize(false));
            else
                System.out.println();
            layer.feed(currentToken, 3);
            if(index % stepSize == stepSize -1)
                layer.updateTreePatterns(outc);

            index++;
        }
        System.out.println();

        printTimeMessage("fed content");
        //layer.updateTreePatterns((index / stepSize) * stepSize);

        layer.updateTreePatterns(outc);
        outc.flush();
        printTimeMessage("updateTreePatterns");

        layer.calcBestPaths();

        /*
        //PrintStream outs = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outs.txt"))), true, "UTF-8");
        layer.printMaximalTreesWithTreeParts(System.out);
        //outs.flush();
        printTimeMessage("printMaximalTreesWithTreeParts");

*/

       /*
        layer.calculateTreePatternProbabilities();
        printTimeMessage("calculateTreePatternProbabilities");

        PrintStream outa = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outa.txt"))), true, "UTF-8");
        layer.printTreePatternsSortedByKey(outa);
        outa.flush();
        printTimeMessage("printProbabilities");


        PrintStream outb = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outb.txt"))), true, "UTF-8");
        layer.printProbabilitiesSortedByValue(outb);
        outb.flush();
        printTimeMessage("printProbabilitiesSortedByValue to file");

        //new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("out.txt"))), true, "UTF-8");
        layer.printProbabilitiesSortedByValueAndKey(out);
        out.flush();
        printTimeMessage("printProbabilitiesSortedByValueAndKey to file");
*/

    }

    @Test
    //@Ignore
    public void filesEqualTest() throws Exception{

        int run = 12;

        File[] listOfFiles = (new File(".")).listFiles();

        ArrayList<BufferedReader> readers = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();

        for(File file: listOfFiles){
            if(file.getName().startsWith("out"+run)){
                files.add(file);
                readers.add(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
                System.out.println(file.getName());
            }

        }

        int linecount = 1;
        String[] lines = new String[readers.size()];

        read:
            while(true){
                int li =0;
                for (BufferedReader reader : readers) {
                    lines[li] = reader.readLine();
                    if(lines[li]==null){
                        System.out.println("read lines: "+linecount);
                        break read;
                    }
                    li++;
                }

                for(int i=0; i<lines.length-1;i++){
                   for(int j=i+1; j<lines.length; j++){
                       if(!lines[i].equals(lines[j])){
                           System.out.println("line: "+linecount+"\t"+lines[i]+" != "+lines[j]+"\t\n("+files.get(i).getName()+" != "+files.get(j).getName()+")");
                           break read;
                       }
                   }
                }

                linecount++;

            }


    }
}
