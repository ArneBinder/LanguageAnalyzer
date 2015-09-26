package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.MultiSet;
import abinder.langanalyzer.helper.MultiTreeSet;

import java.io.PrintStream;
import java.lang.*;
import java.util.*;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayer {

    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    // ATTENTION: parents aren't set!
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees = new ArrayList<>();
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    // with set parents
    MultiTreeSet treePatterns = new MultiTreeSet();
    HashMap<LinguisticTree, Double> probabilities = new HashMap<>();
    HashMap<String, Double> probabilities2 = new HashMap<>();

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

        count++;
    }


    public void updateTreePatterns(){
        for(int i=processedTreesIndex-posOffset; i < previousTrees.size(); i++){
            for(ArrayList<LinguisticTree> trees: previousTrees.get(i)){
                for(LinguisticTree tree: trees) {
                    for (LinguisticTree cutTree : tree.getAllCutTrees()) {
                        //cutTree.setParents(null);
                        treePatterns.add(cutTree);
                    }
                }
            }
            processedTreesIndex++;
        }
    }

    public double getProbability(LinguisticTree tree, LinguisticTree currentPos) {
        //tabs += "\t";
        //System.out.println(tabs + tree.serialize(false) + "\t"+ currentPos.serialize(false) + "\tVISIT");

        double probability = 0;

        // TODO: fix caching!!
        if(tree.equals(currentPos)) {
            //String s = tree.serialize(false);
            if(probabilities.containsKey(tree)) {
                //System.out.println(tabs+tree.serialize(false)+"\t"+currentPos.serialize(false)+ "\tCACHED\t"+ probabilities.get(tree));
                //tabs = tabs.substring(1);
                return probabilities.get(tree);
            }else
                probability += treePatterns.getProbability(tree);
        }

        LinguisticTree leftChild = currentPos.getLeftChild();
        LinguisticTree rightChild = currentPos.getRightChild();

        if(leftChild != null && leftChild.getSize() > 1){
            //System.out.println(tabs + tree.serialize(false) + "\t"+ leftChild.serialize(false) + "\tLEFTCHILD");
            probability += getProbability(tree, leftChild);
        }

        if(rightChild != null && rightChild.getSize() > 1){
            //System.out.println(tabs + tree.serialize(false) + "\t"+ rightChild.serialize(false) + "\tRIGHTCHILD");
            probability += getProbability(tree, rightChild);
        }

        // cut tree
        if(leftChild!=null && rightChild != null){
            // cut left child
            leftChild = currentPos.deleteLeftChild();
            //System.out.println(tabs+leftChild.serialize(false)+ "\to\t"+  tree.serialize(false) + "\tCUT left\t");

            probability += getProbability(leftChild, leftChild) * getProbability(tree, tree);
            currentPos.setLeftChild(leftChild);

            // cut right child
            rightChild = currentPos.deleteRightChild();
            //System.out.println(tabs+tree.serialize(false)+ "\to\t"+ rightChild.serialize(false)+"\t"+"\tCUT right\t");

            probability += getProbability(rightChild, rightChild) * getProbability(tree, tree);
            currentPos.setRightChild(rightChild);
        }

        //System.out.println(tabs+tree.serialize(false)+"\t"+currentPos.serialize(false)+ "\tRETURN\t"+ probability);

        //tabs = tabs.substring(1);


        //if(probability > 0)
        if(tree.equals(currentPos))
            probabilities.put(tree.copyThis(), probability);
        //else {
            //if(probability > 0)
            //    System.out.println("BLAAAA");
        //}
        return probability;
    }

    public void calculateTreePatternProbabilities(){
        for(LinguisticTree tree: treePatterns.keySet()){
            getProbability(tree, tree);
        }
    }

    public ArrayList<LinguisticTree> sortedTrees(ArrayList<LinguisticTree> trees){
        ArrayList<LinguisticTree> result = new ArrayList<>(trees);
        Collections.sort(result);
        return result;
    }


    public void printMaximalTreesWithProbability(PrintStream out){
        out.println("print maximal trees with probaility:");
        int count = 0;
        ArrayList<ArrayList<LinguisticTree>> tempTrees = previousTrees.get(previousTrees.size() - 1);
        for(ArrayList<LinguisticTree> tempTrees1: tempTrees){
            for(LinguisticTree tree: tempTrees1){
                if(tree.getLeftPosition()==0){
                    tree.setParents(null);

                    out.println(tree.serialize(false)+"\t"+getProbability(tree, tree));

                    // print cutTrees of maximal trees
                    //for(LinguisticTree cutTree: sortedTrees(tree.getAllCutTrees())){
                    //    System.out.println("\t"+cutTree.serialize(false));
                    //}

                    count++;
                }
            }
        }
        out.println("count: " + count);
    }

    public void printProbabilities(PrintStream out){
        out.println("print probabilities:");
        for(LinguisticTree tree: probabilities.keySet()){
            out.println(tree.serialize(false)+"\t"+probabilities.get(tree));
        }
        out.println("probabilities.size: " + probabilities.size());
    }

    /*public void printProbabilitiesSortedByValue(PrintStream out){
        out.println("print probabilities (sortedByValue):");
        for(LinguisticTree tree: probabilities.keySet()){//MultiSet.sortByValue(probabilities).keySet()){
            if(!tree.serialize(false).contains("X"))
                out.println(probabilities.get(tree)+"\t"+tree.serialize(false));
        }
    }*/

    public void printProbabilitiesSortedByValue2(PrintStream out){
        out.println("print probabilities (sortedByValue):");
        for(String tree: probabilities2.keySet()){//MultiSet.sortByValue(probabilities).keySet()){
            if(!tree.contains("X"))
                out.println(probabilities2.get(tree)+"\t"+tree);
        }
    }

    public void printProbabilitiesSortedByValue(PrintStream out){
        out.println("print treePatterns (sortByValue)");
        for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+(probabilities.containsKey(treePart)?probabilities.get(treePart):"NULL"));
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: "+probabilities.size());

    }

    public void printProbabilitiesSortedByValueAndKey(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, LinguisticTree>> sortedSet = new TreeSet<>();
        for(Map.Entry<LinguisticTree,Double> entry: probabilities.entrySet()){
            //if(!entry.getKey().serialize(false).contains("X"))
                sortedSet.add(new KeyValuePair<>(entry.getValue(), entry.getKey()));
        }
        for (KeyValuePair<Double, LinguisticTree> keyValuePair : sortedSet) {
            out.println(keyValuePair.key + "\t" + keyValuePair.value.serialize(false));
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getProbability(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: " + probabilities.size());
        out.println("sortedSet.size: " + sortedSet.size());

    }

    public void printProbabilitiesSortedByValueAndKey2(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, String>> sortedSet = new TreeSet<>();
        for(Map.Entry<String,Double> entry: probabilities2.entrySet()){
            //if(!entry.getKey().serialize(false).contains("X"))
            sortedSet.add(new KeyValuePair<>(entry.getValue(), entry.getKey()));
        }
        for (KeyValuePair keyValuePair : sortedSet) {
            out.println(keyValuePair.key+"\t"+keyValuePair.value);
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getProbability(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());

    }

    public void printProbabilitiesSortedByValueAndKey3(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, String>> sortedSet = new TreeSet<>();
        for(Map.Entry<LinguisticTree,Double> entry: probabilities.entrySet()){
            //if(!entry.getKey().serialize(false).contains("X"))
            sortedSet.add(new KeyValuePair<>(entry.getValue(), entry.getKey().serialize(false)));
        }
        for (KeyValuePair keyValuePair : sortedSet) {
            out.println(keyValuePair.key+"\t"+keyValuePair.value);
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getProbability(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());

    }

    public void printTreePatternsSortedByKey(PrintStream out){
        out.println("print treePatterns (sortByKey)");
        for(LinguisticTree treePart: treePatterns.sortedKeySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false));
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("treePatterns.size (total): " + treePatterns.getTotalCount());
    }

    public MultiSet<LinguisticTree> getTreePatterns() {
        return treePatterns;
    }

    public HashMap<LinguisticTree, Double> getProbabilities() {
        return probabilities;
    }

}
class KeyValuePair<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<KeyValuePair<K, V>>{

    K key;
    V value;

    public KeyValuePair(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }



    @Override
    public int compareTo(KeyValuePair<K, V> o) {
        if(o==null)
            return 1;
        //return key==o.key?value.compareTo(o.value):(int)Math.signum(key-o.key);
        int comp = key.compareTo(o.key);
        return (comp==0)?value.compareTo(o.value):comp;
    }
}
