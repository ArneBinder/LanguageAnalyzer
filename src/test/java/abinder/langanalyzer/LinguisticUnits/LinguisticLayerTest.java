package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.*;
import org.junit.Test;

import java.io.*;
import java.lang.*;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

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
    public void deserializationTest() throws Exception {
        LinguisticTree newTree = new LinguisticTree("[\\X,[TREE[e,[a,TREE]],CHR[m,e]]]");
        System.out.println(newTree);
        assertEquals("[\\X,[[e,[a,TREE]],CHR[m,e]]]", newTree.serialize());

        newTree = new LinguisticTree("[\\X,[TREE[e,[CHR[a],k]],CHR[m,TREE[e]]]]");
        System.out.println(newTree);
        assertEquals("[\\X,[[e,[CHR[a],k]],CHR[m,e]]]", newTree.serialize());
    }

    @Test
    public void simpleLayerTest() throws Exception {
        LinguisticLayer layer = new LinguisticLayer(3,4);
        LinguisticTree tree = new LinguisticTree("[ä,[[TREE,TREE],i]]");
        System.out.println(tree.serializeLeafs());
        //LinguisticTree tree = new LinguisticTree("a");
        for(LinguisticTree subTree: tree.getAllSubtrees(layer.getMaxHeight(), new LinkedList<>())){
            System.out.println("\nSUB: "+subTree);
            for(LinguisticTree cutTree: subTree.getAllCutTrees()) {

               //f(cutTree!=null) {
                   //if(!cutTree.noChildren() || cutTree.getLeaf()!=null){
                       layer.addTreePattern(cutTree);
                       System.out.println(cutTree.serialize());
                   //}else
                     //  System.out.println("FALSE");

               /*}
               else {
                   layer.addTreePattern(new LinguisticTree(LinguisticType.TREE));
                   System.out.println("NULL");
               }*/

            }
        }



        //ArrayList<String> str = new ArrayList<>();
        //System.out.println(layer.getProb(tree));
        /*for(String s:layer.getPartitions(tree)){
            System.out.println(s);
        }*/

        Disjunction<LinguisticTree> partitions = tree.getPartitions(new ReconnectedMultiTreeSet(0));
        //layer.addAllTreePattern(partitions.collectTerminals());
        System.out.println("\npartitions:");
        for(Proposition<LinguisticTree> proposition : partitions.getPropositions()){
            System.out.println(proposition +"\t"+ proposition.calculate(layer.getTreePatterns()));
        }

        for(LinguisticTree partitionTree: partitions.getTerminals()){
            System.out.println(partitionTree+"\t"+layer.getTreePatterns().getRelFrequ(partitionTree));
        }

        //System.out.println(partitions);

        //Disjunction sum = layer.getPropositions(tree);
        //System.out.println(sum.calculate(layer.getTreePatterns()));

        //sum.deepFlatten();



        //System.out.println(sum);
        //String partitionString = sum.toString();
        //String[] partitions = partitionString.substring(1, partitionString.length()-1).split(" \\+ ");
        //Arrays.sort(partitions);
        //System.out.println("\npartitions:\n"+String.join("\n", Arrays.asList(partitions))+"\n");

        //System.out.println("\npartitions:\n"+partitions.toString().replaceAll(" \\+ ", "\n") + "\n");

        System.out.println("probability:\t"+ partitions.calculate(layer.getTreePatterns()));
        System.out.println("Done");

    }

    @Test
    public void layerTest() throws IOException, InterruptedException {

        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache_short.txt");
        //corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Syntax.txt");
        printTimeMessage("corpus read");

        PrintStream outc = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outc.txt"))), true, "UTF-8");
        LinguisticLayer layer = new LinguisticLayer(3, corpus.getSize());//16);//
        String simpleInput = "abcdefg";
        //Iterator<Character> characters = new CharacterIterator(simpleInput);
        Iterator<Character> characters = corpus.tokens();

        /*System.out.println("length: "+ simpleInput.length());
        System.out.println("getCatalan: "+Utils.getCatalan(simpleInput.length()));
        System.out.println("getSparseTreeCount: "+Utils.getSparseTreeCount(simpleInput.length()));
        System.out.println("getTreeCount: "+Utils.getTreeCount(simpleInput.length()));
        */
        int index = 0;
        int stepSize = 10;
        System.out.println();
        while(characters.hasNext()){
            char character = characters.next();
            //LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(LinguisticToken.escape(character+""));
            //if(!currentToken.serialize().equals("\\n"))
                System.out.print(currentToken.serialize());
            //else
            //    System.out.println();
            layer.feed(currentToken);

            if(index % stepSize == stepSize -1)
                layer.updateTreePatterns();

            index++;
        }


        System.out.println();
        layer.updateTreePatterns();
        outc.flush();
        printTimeMessage("updateTreePatterns");
        System.out.println("real pattern size(hashmap): "+layer.getTreePatterns().size());
        System.out.println("tree pattern totalCount: "+layer.getTreePatterns().getTotalCount());
        //layer.updateTreePatternsSimple();

        //System.out.println("treePatterns.size: "+layer.treePatterns.size());
        

        System.out.println("t1: "+LinguisticLayer.t1);
        System.out.println("t2: "+LinguisticLayer.t2);
        System.out.println();
        System.out.println("t1: "+LinguisticTree.t1);
        System.out.println("t2: "+LinguisticTree.t2);
        System.out.println("t3: "+LinguisticTree.t3);
        System.out.println("t4: "+LinguisticTree.t4);
        System.out.println("t5: "+LinguisticTree.t5);
        System.out.println("t6: "+LinguisticTree.t6);
        System.out.println("t7: "+LinguisticTree.t7);
        System.out.println("t8: "+LinguisticTree.t8);
        System.out.println("t9: "+LinguisticTree.t9);
        System.out.println("t10: "+LinguisticTree.t10);
        System.out.println();

        System.out.println("c1: "+LinguisticTree.c1);
        layer.calculateTreePatternProbabilities();
        printTimeMessage("calculateTreePatternProbabilities");

/*
        PrintStream outt = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outt.txt"))), true, "UTF-8");

        for(LinguisticTree tree:layer.getTreePatterns()){
            outt.print("\t"+tree.serialize(false));
        }
        for(LinguisticTree treea:layer.getTreePatterns()){
            outt.print("\n"+treea.serialize(false));
            //outt.println();
            for(LinguisticTree treeb:layer.getTreePatterns()){
                outt.print("\t"+treea.getCosineSimilarity(treeb));
                //outt.print("\t"+treeb.serialize(false));//+" X "+treeb.serialize(false));
            }
        }
        outt.flush();
        printTimeMessage("print similarities");
*/
        /*
        //PrintStream outs = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outs.txt"))), true, "UTF-8");
        layer.printMaximalTreesWithTreeParts(System.out);
        //outs.flush();
        printTimeMessage("printMaximalTreesWithTreeParts");

*/


        System.out.println();

        /*int iterations = 5;
        for(int i = 0; i < iterations; i++) {
            layer.calcBestPaths();
            printTimeMessage("calcBestPaths");

            layer.processBestPaths(new int[]{0, 1, 2, 3, 4, 5});
            printTimeMessage("processBestPaths");

            layer.calculateTreePatternProbabilities();
            printTimeMessage("calculateTreePatternProbabilities");
        }*/

/*
        layer.calcBestPaths();
        PrintStream outd = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outd.txt"))), true, "UTF-8");
        layer.printBestPaths(new int[]{0},outd);
        outd.flush();
        printTimeMessage("printBestPaths");
*/
/*
        PrintStream outa = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outa.txt"))), true, "UTF-8");
        layer.printProbabilitiesSortedByValueAndKey(outa);
        outa.flush();
        printTimeMessage("printProbabilities");

        PrintStream outd = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outd.txt"))), true, "UTF-8");
        try {
            layer.printBestPaths(new int[]{0, 1, 2, 3, 4, 5}, outd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outd.flush();
        printTimeMessage("printBestPaths");
*/


        PrintStream outb = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("outb.txt"))), true, "UTF-8");
        layer.printTreePatternsSortedByKey(outb);
        outb.flush();
        printTimeMessage("printTreePatternsSortedByKey to file");

        //new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File("out.txt"))), true, "UTF-8");
        layer.printProbabilitiesSortedByValueAndKey(out);
        out.flush();
        printTimeMessage("printProbabilitiesSortedByValueAndKey to file");


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
