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

import java.util.HashMap;
import java.util.Iterator;

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

        ImmutableList<Character> chars = Lists.charactersOf("schöne");
        UnmodifiableListIterator<Character> iter = chars.listIterator();

        String dummy = "";
        Iterator<Character> tokens = new CharacterIterator("das");
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

        HashMap<LinguisticTree, Integer> trees = new HashMap<>();
        int totalcount = 0;
        String out = "";
        for(LinguisticTree tree: globalToken.getAllTrees(4)){

            System.out.println(tree.serialize(true));
            for(LinguisticTree subTree: tree.getAllSubtrees(4)){
                System.out.println("\t"+subTree.serialize(true));
                for(LinguisticTree cutTree: subTree.getAllCutTrees(0)){
                    //String t = cutTree.serialize(true);
                    System.out.println("\t\t"+cutTree.serialize(true));
                    int count = 0;
                    if(trees.containsKey(cutTree)){
                        count = trees.get(cutTree);
                    }
                    trees.put(cutTree,count + 1);
                    totalcount++;
                }
                //System.out.println(cutTree.serialize() + "\t" + cutTree.getDepth());
            }




            out += tree.serialize(true)+"\t"+tree.getDepth()+"\n";
            //System.out.println(tree.serialize() + "\t" + tree.getDepth());
            //lastTree = tree;
        }
        System.out.println();

        System.out.println("diffSize: \t"+trees.size());
        System.out.println("totalcount: \t"+totalcount);

        for(LinguisticTree tree: trees.keySet()){
            System.out.println(tree.serialize(false)+"\t" + trees.get(tree));
        }
        //String out = globalToken.serialize();
        //System.out.println(out);
        IO.writeFile("test.txt", out);

    }

}
