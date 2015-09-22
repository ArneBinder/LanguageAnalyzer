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
                for (LinguisticTree cutTree : tree.getAllCutTrees()) {
                    treeParts.add(cutTree);
                }
            }
        }

        count++;
    }


    public void checkTrees(){
        System.out.println("checkTrees");
        MultiSet<LinguisticTree> trees = new MultiSet<>();


        /*
        int count = 0;
        ArrayList<ArrayList<LinguisticTree>> tempTrees = previousTrees.get(previousTrees.size()-1);
        for(ArrayList<LinguisticTree> tempTrees1: tempTrees){
            for(LinguisticTree tree: tempTrees1){
                if(tree.getLeftPosition()==0){
                    System.out.println(tree.serialize(false));
                    for(LinguisticTree cutTree: tree.getAllCutTrees()){
                        System.out.println("\t"+cutTree.serialize(false));
                    }
                    count++;
                }
            }
        }
        System.out.println("count: "+count);
*/
/*
        for(LinguisticTree tree: fullTrees){
            System.out.println(tree.serialize(false));
        }
        System.out.println("size: "+fullTrees.size());
*/
        /*
        int endPos = 0;
        for(ArrayList<ArrayList<LinguisticTree>> treesSameEnd: previousTrees){
            System.out.println("endpos:\t"+endPos);
            int depth = 0;
            for(ArrayList<LinguisticTree> treesSameDepth: treesSameEnd){
                System.out.println("depth:\t"+depth);
                for(LinguisticTree tree: treesSameDepth){
                    System.out.println(tree.serialize(false));
                    //trees.add(tree);
                }
                depth++;
            }
            endPos++;
        }
        */
/*
        for(LinguisticTree treeStr: trees.keySet()){
            System.out.println(trees.get(treeStr)+"\t"+treeStr.serialize(false));
        }
        System.out.println("trees.size: " + trees.size());
        System.out.println();
        */


        for(LinguisticTree treePart: treeParts.sortByValue().keySet()){
            System.out.println(treeParts.get(treePart)+"\t"+treePart.serialize(false));
        }

        System.out.println("treeParts.size: "+treeParts.size());

    }

}
