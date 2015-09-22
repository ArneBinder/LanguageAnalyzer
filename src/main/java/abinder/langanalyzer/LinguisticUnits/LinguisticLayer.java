package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.MultiSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayer {

    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees = new ArrayList<>();
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    MultiSet<LinguisticTree> treeParts = new MultiSet<>();

    int count = 0;
    int posOffset = 0;

    public LinguisticLayer(){
        //previousTrees.add(new ArrayList<>());

    }

    public void feed(LinguisticToken token, int maxDepth){
        token.setPosition(count);


        // construct trees
        LinguisticTree firstTokenTree = new LinguisticTree(token);
        ArrayList<LinguisticTree> currentTrees = new ArrayList<>(1);
        currentTrees.add(firstTokenTree);
        ArrayList<ArrayList<LinguisticTree>> newTrees = new ArrayList<>();
        newTrees.add(currentTrees);
        //newTrees.add(currentTrees);
        //if(count - posOffset - 1 >= 0) {
        int currentTokenTreesDepth = 0;
        int currentDepth = 0;
        ArrayList<LinguisticTree> currentTokenTrees;

        while(currentTokenTreesDepth < maxDepth && currentTokenTreesDepth < newTrees.size()) {
            currentTokenTrees = newTrees.get(currentTokenTreesDepth);

            ArrayList<LinguisticTree> currentNewTrees = new ArrayList<>();
            for(LinguisticTree currentTokenTree: currentTokenTrees) {

                // iterate over maxDepth of previous trees ending at the previous position (count - posOffset)
                if(currentTokenTree.getLeftPosition() - 1 >= 0) {
                    currentDepth = currentTokenTreesDepth;
                    for (ArrayList<LinguisticTree> currentPreviousTrees : previousTrees.get(currentTokenTree.getLeftPosition() - 1)) {
                        if (currentDepth == maxDepth)
                            break;
                        for (LinguisticTree currentPreviousTree : currentPreviousTrees) {
                            LinguisticTree newTree = new LinguisticTree(currentPreviousTree, currentTokenTree);
                            currentNewTrees.add(newTree);
                        }
                        currentDepth++;
                    }
                }
            }

            for(LinguisticTree currentNewTree: currentNewTrees){
                int depth = currentNewTree.getDepth();
                while(depth >= newTrees.size()){
                    newTrees.add(new ArrayList<>());
                }
                newTrees.get(depth).add(currentNewTree);

            }
            currentTokenTreesDepth++;
        }
        previousTrees.add(newTrees);
        // construct trees END

        for(ArrayList<LinguisticTree> trees: newTrees){
            for(LinguisticTree tree: trees) {
                for (LinguisticTree cutTree : tree.getAllCutTrees(10)) {
                    treeParts.add(cutTree);
                }
            }
        }

        count++;
    }


    public void checkTrees(){
        System.out.println("checkTrees");
        MultiSet<LinguisticTree> trees = new MultiSet<>();

        for(ArrayList<ArrayList<LinguisticTree>> treesSameEnd: previousTrees){
            for(ArrayList<LinguisticTree> treesSameDepth: treesSameEnd){
                for(LinguisticTree tree: treesSameDepth){
                    trees.add(tree);
                }
            }
        }
        for(LinguisticTree treeStr: trees.keySet()){
            System.out.println(trees.get(treeStr)+"\t"+treeStr.serialize(false));
        }
        System.out.println("trees.size: " + trees.size());
        System.out.println();

        MultiSet<LinguisticTree> cutTrees = new MultiSet<>();
        for(LinguisticTree treePart: treeParts.sortByValue().keySet()){
            cutTrees.add(treePart);
            //System.out.println(treePart);
        }

        for(LinguisticTree treePart: cutTrees.sortByValue().keySet()){
            System.out.println(cutTrees.get(treePart)+"\t"+treePart.serialize(false));
        }

        System.out.println("treeParts.size: "+treeParts.size());
    }

}
