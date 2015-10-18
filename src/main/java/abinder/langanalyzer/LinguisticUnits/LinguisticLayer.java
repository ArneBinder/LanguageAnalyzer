package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.*;

import java.io.PrintStream;
import java.lang.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayer {

    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    // ATTENTION: parents aren't set!
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees = new ArrayList<>();

    ArrayList<LinguisticTree>[] bestTrees;
    double[] bestProbabilities;
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    // with set parents
    MultiTreeSet treePatterns = new MultiTreeSet();
    HashMap<LinguisticTree, Double> probabilities = new HashMap<>();
    private HashMap<LinguisticTree, Sum> partitions;

    int maxDepth = 3;
    int count = 0;
    int posOffset = 0;
    int processedTreesIndex = 0;
    String tabs = "";

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public LinguisticLayer(int maxDepth){
        this.maxDepth = maxDepth;
        //previousTrees.add(new ArrayList<>());

    }

    public void feed(LinguisticToken token){
        token.setPosition(count);


        // construct trees
        LinguisticTree firstTokenTree = new LinguisticTree(token, LinguisticType.TREE);
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
                            LinguisticTree newTree = new LinguisticTree(currentPreviousTree, currentTokenTree, LinguisticType.TREE);
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


    public void updateTreePatterns(PrintStream out){
        double threshold = 1.5E-3;

        for(int i=processedTreesIndex-posOffset; i < previousTrees.size(); i++){
            for(ArrayList<LinguisticTree> trees: previousTrees.get(i)){
                for(LinguisticTree tree: trees) {
                    probabilities.clear();
                    tree.setParents(null);
                    //double probability = getProb(tree);//getProbabilityForHead(tree, tree, new LinguisticTree[0]);
                    //if(treePatterns.size() < 20000 ||  probability > threshold) {

                    //out.println(probability + "\t" + tree.serialize(false) + "\t" + treePatterns.getTotalCount()+"\t"+treePatterns.size());

                        //addAllTreePattern(tree.getAllCutTrees());
                    addAllTreePattern(tree.calcPartitions().collectTerminals());
                        /*for (LinguisticTree cutTree : tree.getTreeParts(tree)){//tree.getAllCutTrees()) { //
                            //cutTree.setParents(null);
                            treePatterns.add(cutTree);
                        }*/
                    //}
                }
            }
            processedTreesIndex++;
        }
    }

    public void addAllTreePattern(List<LinguisticTree> trees){
        for(LinguisticTree tree: trees){
            treePatterns.add(tree);
        }
    }

    public void addTreePattern(LinguisticTree tree){
        treePatterns.add(tree);
    }


    public void calcBestPaths(){
        if(bestProbabilities == null)
            bestProbabilities = new double[previousTrees.size()+1]; //new ArrayList<>(previousTrees.size());
        if(bestTrees==null)
            bestTrees = (ArrayList<LinguisticTree>[]) Array.newInstance(ArrayList.class, previousTrees.size());//(new ArrayList[previousTrees.size()]); //new ArrayList<>(previousTrees.size());


       for(int i=previousTrees.size()-1; i>=0; i--){
           for(ArrayList<LinguisticTree> currentTrees: previousTrees.get(i)){
               for(LinguisticTree tree: currentTrees){
                   double newProb = Math.log(tree.calcPartitions().calculate(treePatterns))+bestProbabilities[i+1];
                   int newPos = tree.getLeftPosition();
                   if(bestProbabilities[newPos] == 0.0 || newProb >= bestProbabilities[newPos]){

                       if(newProb != bestProbabilities[newPos])
                           bestTrees[newPos] = new ArrayList<>();
                       bestProbabilities[newPos]= newProb;
                       bestTrees[newPos].add(tree);
                   }

               }
           }


       }

        //System.out.println("end");


    }


    public void processBestPaths(int[] startIndices) throws InterruptedException {
        LinkedBlockingQueue<Integer> indices = new LinkedBlockingQueue<>();
        for(int index: startIndices) {
            indices.put(index-posOffset);
        }
        int newPos;

        probabilities.clear();
        treePatterns.clear();

        while(!indices.isEmpty()){
            for (LinguisticTree tree : bestTrees[indices.poll()]) {
                // do sth with tree
                //out.println(tree.serialize(false));
                for(LinguisticTree subTree:tree.getAllSubtrees(maxDepth)){
                    addAllTreePattern(subTree.getAllCutTrees());
                }
                //out.flush();
                newPos = tree.getRightPosition() + 1;
                if(newPos < bestTrees.length)
                    indices.put(newPos);
            }
        }
        //System.out.println("finished");
    }

    public void printBestPaths(int[] startIndices, PrintStream out) throws InterruptedException {
        LinkedBlockingQueue<Integer> indices = new LinkedBlockingQueue<>();
        for(int index: startIndices) {
            indices.put(index-posOffset);
        }
        int newPos;
        while(!indices.isEmpty()){
            for (LinguisticTree tree : bestTrees[indices.poll()]) {
                // do sth with tree
                out.println(tree.serialize());

                //out.flush();
                newPos = tree.getRightPosition() + 1;
                if(newPos < bestTrees.length)
                    indices.put(newPos);
            }
        }
        //System.out.println("finished");
    }


    public void calculateTreePatternProbabilities(){
        for(LinguisticTree tree: treePatterns.keySet()){
            //System.out.println(tree.serialize(false));
            //if(tree.serialize(false).equals("[[a,b],[c,d]]")) {
                //getProbability(tree, tree, tree.getLeafCount()-1);
                //tree.setParents(null);
                //getProb(tree);

                probabilities.put(tree,tree.calcPartitions().calculate(getTreePatterns()));
                //getProbabilityForHead(tree, tree, new LinguisticTree[0]);
            //}
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
                    //tree.setParents(null);

                    out.println(tree.serialize()+"\t"+tree.calcPartitions().calculate(treePatterns));

                    // print cutTrees of maximal trees
                    //for(LinguisticTree cutTree: sortedTrees(tree.getAllCutTreesInclusiveThis())){
                    //    System.out.println("\t"+cutTree.serialize(false));
                    //}

                    count++;
                }
            }
        }
        out.println("count: " + count);
    }


    public void printMaximalTreesWithTreeParts(PrintStream out){
        out.println("print maximal trees with probaility:");
        int count = 0;
        ArrayList<ArrayList<LinguisticTree>> tempTrees = previousTrees.get(previousTrees.size() - 1);
        for(ArrayList<LinguisticTree> tempTrees1: tempTrees){
            for(LinguisticTree tree: tempTrees1){
                if(tree.getLeftPosition()==0){
                    //tree.setParents(null);

                    out.println(tree.serialize());
                    System.out.println(tree.calcPartitions().calculate(treePatterns));
                    //tree.setParents(null);
                    //tree.getTreeParts(tree);

                    //for(LinguisticTree treePart:tree.getTreeParts(tree)){
                        //out.println("\t"+treePart.serialize(false));
                    //}

                    // print cutTrees of maximal trees
                    //for(LinguisticTree cutTree: sortedTrees(tree.getAllCutTreesInclusiveThis())){
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
            out.println(tree.serialize()+"\t"+probabilities.get(tree));
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



    public void printProbabilitiesSortedByValue(PrintStream out){
        out.println("print treePatterns (sortByValue)");
        for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize() + "\t"+(probabilities.containsKey(treePart)?probabilities.get(treePart):"NULL"));
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: "+probabilities.size());

    }

    public void printProbabilitiesSortedByValueAndKey(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, LinguisticTree>> sortedSet = new TreeSet<>();
        for(Map.Entry<LinguisticTree,Double> entry: probabilities.entrySet()){
            //if(!entry.getKey().serialize(false).contains("X"))
            //if(!entry.getKey().isFull())
                sortedSet.add(new KeyValuePair<>(entry.getValue(), entry.getKey()));
        }
        for (KeyValuePair<Double, LinguisticTree> keyValuePair : sortedSet) {
            out.println(keyValuePair.key + "\t" + keyValuePair.value.serialize());
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getProbability(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: " + probabilities.size());
        out.println("sortedSet.size: " + sortedSet.size());

    }



    public void printProbabilitiesSortedByValueAndKey3(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, String>> sortedSet = new TreeSet<>();
        for(Map.Entry<LinguisticTree,Double> entry: probabilities.entrySet()){
            //if(!entry.getKey().serialize(false).contains("X"))
            sortedSet.add(new KeyValuePair<>(entry.getValue(), entry.getKey().serialize()));
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
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize());
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("treePatterns.size (total): " + treePatterns.getTotalCount());
    }

    public void printTreePatternsSortedByValue(PrintStream out){
        out.println("print treePatterns (sortByValue)");
        for(LinguisticTree treePart: MultiSet.sortByValue(treePatterns).keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize());
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

