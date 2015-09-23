package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.MultiSet;

import java.lang.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayer {

    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees = new ArrayList<>();
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    MultiSet<LinguisticTree> treeParts = new MultiSet<>();
    HashMap<LinguisticTree, Double> probabilities = new HashMap<>();

    int count = 0;
    int posOffset = 0;
    int processedTreesIndex = 0;
    String tabs = "";

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

        /*
        for(ArrayList<LinguisticTree> trees: newTrees){
            for(LinguisticTree tree: trees) {
                for (LinguisticTree cutTree : tree.getAllCutTrees()) {
                    treeParts.add(cutTree);
                }
            }
        }
        */

        count++;
    }


    public void processTrees(){
        for(int i=processedTreesIndex-posOffset; i < previousTrees.size(); i++){
            for(ArrayList<LinguisticTree> trees: previousTrees.get(i)){
                for(LinguisticTree tree: trees) {
                    for (LinguisticTree cutTree : tree.getAllCutTrees()) {
                        treeParts.add(cutTree);
                    }
                }
            }
            processedTreesIndex++;
        }
    }

    public double getProbability(LinguisticTree tree, LinguisticTree currentPos) {
        tabs += "\t";
        //System.out.println(tabs + tree.serialize(false) + "\t"+ currentPos.serialize(false) + "\tVISIT");


        /*
        if(tree.equals(currentPos) && probabilities.containsKey(tree)) {
            System.out.println(tabs+tree.serialize(false)+"\t"+probabilities.get(tree)+"\tFOUND");
            tabs = tabs.substring(1);
            return probabilities.get(tree);
        }*/

        double probability = 0;
        //if(tree.equals(currentPos)) {

        probability += treeParts.getProbability(tree);
            //System.out.println(tabs+tree.serialize(false)+"\t"+probability + "\tSET");
        //}

        ////System.out.println(tabs+"\tSTA\t" + tree.serialize(false) + "\t" +probability);



        LinguisticTree leftChild = currentPos.getLeftChild();
        LinguisticTree rightChild = currentPos.getRightChild();

        if(leftChild != null){
            probability += getProbability(tree, leftChild);
        }

        if(rightChild != null){
            probability += getProbability(tree, rightChild);
        }

        // cut tree
        if(leftChild!=null && rightChild != null){
            LinguisticTree newTreeLeftEmpty = tree.copyThisWithoutChild(leftChild);
            //System.out.println(tabs+"CALC\t" + leftChild.serialize(false)+" o "+newTreeLeftEmpty.serialize(false));
            probability += getProbability(leftChild, leftChild) * getProbability(newTreeLeftEmpty,rightChild);
            LinguisticTree newTreeRightEmpty = tree.copyThisWithoutChild(rightChild);
            //System.out.println(tabs+"CALC\t"+newTreeRightEmpty.serialize(false) + " o "+rightChild.serialize(false));
            probability += getProbability(rightChild, rightChild) * getProbability(newTreeRightEmpty, leftChild);

        }

        /*if(tree.equals(currentPos)){
            System.out.println(tabs+tree.serialize(false)+"\t"+probability+"\tEQUAL");
        }*/

        System.out.println(tabs+tree.serialize(false)+"\t"+currentPos.serialize(false)+"\t"+probability + "\tRETURN");
        tabs = tabs.substring(1);
        if(probability > 0)
            probabilities.put(tree, probability);
        else
            System.out.println("BLAAAA");
        return probability;
    }

    public ArrayList<LinguisticTree> sortedTrees(ArrayList<LinguisticTree> trees){
        ArrayList<LinguisticTree> result = new ArrayList<>(trees);
        Collections.sort(result);
        return result;
    }

    public void checkTrees(){
        System.out.println("checkTrees");
        MultiSet<LinguisticTree> trees = new MultiSet<>();



        System.out.println("print maximal trees:");
        int count = 0;
        ArrayList<ArrayList<LinguisticTree>> tempTrees = previousTrees.get(previousTrees.size() - 1);
        for(ArrayList<LinguisticTree> tempTrees1: tempTrees){
            for(LinguisticTree tree: tempTrees1){
                if(tree.getLeftPosition()==0){
                    System.out.println(tree.serialize(false));
                    System.out.println(getProbability(tree, tree));

                    // print cutTrees of maximal trees
                    //for(LinguisticTree cutTree: sortedTrees(tree.getAllCutTrees())){
                    //    System.out.println("\t"+cutTree.serialize(false));
                    //}

                    count++;
                }
            }
        }
        System.out.println("count: "+count);

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

        System.out.println("print treeParts (sortByKey)");
        for(LinguisticTree treePart: treeParts.sortedKeySet()){
            System.out.println(treeParts.get(treePart)+"\t"+treePart.serialize(false)+"\t"+probabilities.get(treePart));
        }
        System.out.println("treeParts.size: " + treeParts.size());
        System.out.println("treeParts.size (total): " + treeParts.getTotalCount());
        System.out.println();


        /*
        System.out.println("print treeParts (sortByValue)");
        for(LinguisticTree treePart: treeParts.sortByValue().keySet()){
            System.out.println(treeParts.get(treePart)+"\t"+treePart.serialize(false));
        }

        System.out.println("treeParts.size: "+treeParts.size());
*/
    }

}
