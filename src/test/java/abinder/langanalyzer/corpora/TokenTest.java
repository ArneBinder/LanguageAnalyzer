package abinder.langanalyzer.corpora;

import abinder.langanalyzer.LinguisticUnits.LinguisticModel;
import abinder.langanalyzer.LinguisticUnits.LinguisticToken;
import abinder.langanalyzer.LinguisticUnits.LinguisticTree;
import abinder.langanalyzer.LinguisticUnits.LinguisticType;
import abinder.langanalyzer.corpora.wikipedia.WIKIPEDIACorpus;
import abinder.langanalyzer.helper.CharacterIterator;
import abinder.langanalyzer.helper.IO;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Arne on 17.09.2015.
 */
public class TokenTest {

    @Test
    public void tokenTest() throws Exception {
        WIKIPEDIACorpus corpus = new WIKIPEDIACorpus();

        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Sprache.txt");
        corpus.readFromFile("src/test/resources/abinder/langanalyzer/corpora/wikipedia/Syntax.txt");




        LinguisticType globalType = new LinguisticType((char)0);
        LinguisticToken globalToken = new LinguisticToken(globalType);

        //ImmutableList<Character> chars = Lists.charactersOf("schöne Haus");
        //UnmodifiableListIterator<Character> iter = chars.listIterator();

        String dummy = "";
        Iterator<Character> tokens = new CharacterIterator("Das schöne Haus");
        while(tokens.hasNext()){
            char character = tokens.next();
            dummy += character;
            LinguisticType currentType = new LinguisticType(character);
            LinguisticToken currentToken = new LinguisticToken(currentType);
            globalToken.feed(currentToken);
            //model.feed(character);
            //out += character;
            //System.out.print(tokens.next());
        }
        System.out.println(dummy);

        long startTime = System.currentTimeMillis();
        HashMap<LinguisticTree, Integer> trees = new HashMap<>();
        int totalcount = 0;
        String out = "";

        HashMap<String, Integer> patterns = new HashMap<>();
        HashMap<String, Integer> patternsPositioned = new HashMap<>();
        /*for(LinguisticTree tree: LinguisticTree.constructTrees2(globalToken.getTokens(), 4)){
            String t = tree.serialize(false);
            //out += t+"\t"+tree.getDepth()+"\n";
            System.out.println(t+"\t"+tree.getDepth());
            for(LinguisticTree cutTree: tree.getAllCutTrees()){
                String ct = cutTree.serialize(false);
                String ctp = cutTree.serialize(true);
                int count = 0;
                int countp = 0;
                if(patterns.containsKey(ct))
                    count = patterns.get(ct);
                if(patternsPositioned.containsKey(ctp))
                    countp = patternsPositioned.get(ctp);

                count++;
                countp++;
                patterns.put(ct,count);
                patternsPositioned.put(ctp, countp);
                //out += "\t"+ct+"\t"+cutTree.getDepth()+"\n";
                //System.out.println("\t"+ct+"\t"+cutTree.getDepth());
            }
        }
        */

        out +=patterns.size()+"\n\n";
        System.out.println("PATTERNS: \t"+patterns.size()+"\n");

        Map<String, Integer> sortedMap = new TreeMap<>(sortByValue(patterns));
        //System.out.println(sortedMap);
        out += printMap(sortedMap);
        /*for(String pattern: patterns.keySet()){
            System.out.println(pattern+"\t\t"+patterns.get(pattern));
        }*/

        out +=patternsPositioned.size()+"\n\n";
        System.out.println("\nPATTERNPOSITIONED: \t"+patternsPositioned.size()+"\n");
        //out += printMap(patternsPositioned);
        sortedMap = new TreeMap<>(sortByValue(patternsPositioned));
        out += printMap(sortedMap);
        //System.out.println(sortedMap);
        /*for(String pattern: patternsPositioned.keySet()){
            if(patternsPositioned.get(pattern)>1)
                System.out.println(pattern+"\t\t"+patternsPositioned.get(pattern));

        }*/

        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("needed time: " + (endTime - startTime));

        IO.writeFile("test.txt", out);
        /*

        Collection<LinguisticTree> fullTrees = globalToken.getAllTrees(3);
        System.out.println("calculated fullTrees: "+fullTrees.size());
        for(LinguisticTree tree: fullTrees){

            System.out.println(tree.serialize(true));
            for(LinguisticTree subTree: tree.getAllSubtrees(4)){
                //System.out.println("\t"+subTree.serialize(true));
                for(LinguisticTree cutTree: subTree.getAllCutTrees(2)){
                    //System.out.println("\t\t"+cutTree.serialize(true));
                    int count = 0;
                    if(trees.containsKey(cutTree)){
                        count = trees.get(cutTree);
                    }
                    trees.put(cutTree,count + 1);
                    totalcount++;
                }
            }

            //out += tree.serialize(true)+"\t"+tree.getDepth()+"\n";
            //System.out.println(tree.serialize() + "\t" + tree.getDepth());
        }

        long endTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("needed time: "+(endTime-startTime));

        System.out.println();

        System.out.println("diffSize: \t"+trees.size());
        System.out.println("totalcount: \t"+totalcount);

        for(LinguisticTree tree: trees.keySet()){
            out +=tree.serialize(false)+"\tcount: " + trees.get(tree)+"\tdepth: "+tree.getDepth()+"\n";
            //System.out.println(tree.serialize(false)+"\t" + trees.get(tree));
        }
        //String out = globalToken.serialize();
        //System.out.println(out);

*/
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }


    public static String printMap(Map<String, Integer> map){
        String result = "";
        for(Map.Entry entry : map.entrySet()) {
            result += entry.getKey()+"\t\t"+entry.getValue()+"\n";
        }
        return result;
    }

}
