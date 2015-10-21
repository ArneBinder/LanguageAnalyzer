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
    HashMap<Integer, ArrayList<ArrayList<LinguisticTree>>> previousTrees;
    ArrayList<HashMap<Integer, HashSet<LinguisticTree>>> previousTreesBySize;

    ArrayList<LinguisticTree>[] bestTrees;
    double[] bestProbabilities;
    MultiSet<Integer> sequenceProbs;
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    // with set parents
    MultiSet<LinguisticTree> treePatterns;
    HashMap<LinguisticTree, Double> probabilities;
    private HashMap<LinguisticTree, Sum> partitions;

    int maxHeight = 3;
    int feededTokenCount = 0;
    int posOffset = 0;
    int processedTreesIndex = 0;
    int maxTreeSize;
    String tabs = "";

    //just debugging
    public static long t1, t2, t3, t4;

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public LinguisticLayer(int maxHeight, int expectedSize){
        this.maxHeight = maxHeight;
        maxTreeSize = 1 << maxHeight;

        previousTrees = new HashMap<>(expectedSize);
        previousTreesBySize = new ArrayList<>(expectedSize);
        sequenceProbs = new MultiSet<>(expectedSize);



        long expectedPatternSize = Utils.getTreeCount(maxTreeSize) * (expectedSize - maxTreeSize +1);
        System.out.println("expectedPatternSize: "+expectedPatternSize);
        if(expectedPatternSize > Integer.MAX_VALUE)
            System.out.println("ERROR: expectedPatternSize = "+expectedPatternSize+" > Integer.MAX_VALUE = "+Integer.MAX_VALUE);
        treePatterns = new MultiSet<>((int) expectedPatternSize);
        treePatterns.add(new LinguisticTree(LinguisticType.TREE));
        // set position zero to 100%
        sequenceProbs.add(0);
    }

    public void feed(LinguisticToken token){
        token.setPosition(feededTokenCount);


        // construct trees
        LinguisticTree firstTokenTree = new LinguisticTree(token, LinguisticType.TREE);
        ArrayList<LinguisticTree> currentTrees = new ArrayList<>(1);
        currentTrees.add(firstTokenTree);
        ArrayList<ArrayList<LinguisticTree>> newTrees = new ArrayList<>();
        newTrees.add(currentTrees);
        //newTrees.add(currentTrees);
        //if(feededTokenCount - posOffset - 1 >= 0) {
        int currentTokenTreesHeight = 0;
        int currentHeight = 0;
        ArrayList<LinguisticTree> currentTokenTrees;

        while(currentTokenTreesHeight < maxHeight && currentTokenTreesHeight < newTrees.size()) {
            currentTokenTrees = newTrees.get(currentTokenTreesHeight);

            ArrayList<LinguisticTree> currentNewTrees = new ArrayList<>();
            for(LinguisticTree currentTokenTree: currentTokenTrees) {

                // iterate over maxHeight of previous trees ending at the previous position (feededTokenCount - posOffset)
                if(currentTokenTree.getLeftPosition() - 1 >= 0) {
                    currentHeight = currentTokenTreesHeight;
                    for (ArrayList<LinguisticTree> currentPreviousTrees : previousTrees.get(currentTokenTree.getLeftPosition() - 1)) {
                        if (currentHeight == maxHeight)
                            break;
                        for (LinguisticTree currentPreviousTree : currentPreviousTrees) {
                            LinguisticTree newTree = new LinguisticTree(currentPreviousTree, currentTokenTree, LinguisticType.TREE);
                            currentNewTrees.add(newTree);
                        }
                        currentHeight++;
                    }
                }
            }

            for(LinguisticTree currentNewTree: currentNewTrees){
                int depth = currentNewTree.getHeight();
                while(depth >= newTrees.size()){
                    newTrees.add(new ArrayList<>());
                }
                newTrees.get(depth).add(currentNewTree);

            }
            currentTokenTreesHeight++;
        }

        HashMap<Integer, HashSet<LinguisticTree>> sortedBySize = new HashMap<>();

        for(ArrayList<LinguisticTree> trees: newTrees){
            for(LinguisticTree tree: trees){
                int size = tree.getSize();
                HashSet<LinguisticTree> current = sortedBySize.get(size);
                try{
                    current.add(tree);
                }catch(NullPointerException e){
                    current = new HashSet<>();
                    current.add(tree);
                }
                sortedBySize.put(size, current);
            }

        }
        previousTreesBySize.add(sortedBySize);
        if(previousTrees.size() > maxTreeSize){
            previousTrees.remove(feededTokenCount - maxTreeSize -1);
        }
        previousTrees.put(feededTokenCount, newTrees);


        // construct trees END

        feededTokenCount++;
    }

    public void updateTreePatternsSimple(){
        for(int endPos = processedTreesIndex-posOffset; endPos < previousTreesBySize.size(); endPos++){
            for(Integer size: previousTreesBySize.get(endPos).keySet()){
                for(LinguisticTree treeFixedPosSize: previousTreesBySize.get(endPos).get(size)) {
                    addAllTreePattern(treeFixedPosSize.getPartitions().collectTerminals());
                }
            }
            processedTreesIndex++;
        }

    }

    public void updateTreePatterns(){
        long start1, start2;

        for(int endPos = processedTreesIndex-posOffset; endPos < previousTreesBySize.size(); endPos++){
            start1 = System.currentTimeMillis();

            for(Integer size: previousTreesBySize.get(endPos).keySet()){
                double sizeRelFrequ = 0;
                for(LinguisticTree treeFixedPosSize: previousTreesBySize.get(endPos).get(size)) {
                    double relFrequ = treeFixedPosSize.getPartitions().calculate(treePatterns);
                    sizeRelFrequ += relFrequ;
                    treeFixedPosSize.setProbability(relFrequ);
                }
                double newSequProb = sequenceProbs.get(processedTreesIndex + 1 - size)* sizeRelFrequ;
                sequenceProbs.add(processedTreesIndex + 1, newSequProb);//>=1.0?1.0:newSequProb);
            }
            t1+=System.currentTimeMillis()-start1;
            start2 = System.currentTimeMillis();
            int addPos = processedTreesIndex + 1 - (1<<(maxHeight -1));
            if(addPos >= 0){
                double probSum = 0;
                for(int size = 1; size <= (1<<(maxHeight -1)) && addPos >= size -1; size++){
                    for(int tempEndPos = addPos; tempEndPos < addPos + size; tempEndPos++){
                        double prevSequProb = sequenceProbs.get(tempEndPos - size +1);
                        for(LinguisticTree tree: previousTreesBySize.get(tempEndPos).get(size)) {
                            probSum += prevSequProb*tree.getProbability();
                        }
                    }
                }

                for(int size = 1; size <= (1<<(maxHeight -1)) && addPos >= size -1; size++){
                    //for(int tempEndPos = addPos; tempEndPos < addPos + size; tempEndPos++){
                        double prevSequProb = sequenceProbs.get(addPos - size +1);
                        for(LinguisticTree tree: previousTreesBySize.get(addPos).get(size)) {
                            double currentProb = prevSequProb*tree.getProbability() / probSum;
                            for(LinguisticTree part: tree.getPartitions().collectTerminals()) {
                                treePatterns.add(part, currentProb);
                            }
                        }
                    //}
                }

            }
            t2+=System.currentTimeMillis()-start2;
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


    public void calcBestPaths() {
        bestProbabilities = new double[previousTreesBySize.size() + 1]; //new ArrayList<>(previousTrees.size());
        bestTrees = (ArrayList<LinguisticTree>[]) Array.newInstance(ArrayList.class, previousTreesBySize.size());//(new ArrayList[previousTrees.size()]); //new ArrayList<>(previousTrees.size());

        for (int endPos = previousTreesBySize.size() - 1; endPos >= 0; endPos--) {
            HashMap<Integer, HashSet<LinguisticTree>> currentTrees = previousTreesBySize.get(endPos);
            for (int size : currentTrees.keySet()) {
                HashSet<LinguisticTree> trees = currentTrees.get(size);

                LinguisticTree bestTree = null;
                double bestProb = 0;
                //double sumProb = 0;
                for (LinguisticTree tree : trees) {
                    double currentProb = tree.getPartitions().calculate(treePatterns);
                    //sumProb += currentProb;
                    if (currentProb > bestProb) {
                        bestProb = currentProb;
                        bestTree = tree;
                    }
                }

                double newProb = Math.log(bestProb) + bestProbabilities[endPos + 1];
                int newPos = bestTree.getLeftPosition();
                if (bestProbabilities[newPos] == 0.0 || newProb >= bestProbabilities[newPos]) {

                    if (bestTrees[newPos] == null)//newProb != bestProbabilities[newPos])
                        bestTrees[newPos] = new ArrayList<>();
                    bestProbabilities[newPos] = newProb;
                    bestTrees[newPos].add(bestTree);
                }
            }
        }
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
                for(LinguisticTree subTree:tree.getAllSubtrees(maxHeight)){
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
        MultiSet<String> pseudoWords = new MultiSet<>(previousTreesBySize.size());
        LinkedBlockingQueue<Integer> indices = new LinkedBlockingQueue<>();
        for(int index: startIndices) {
            indices.put(index-posOffset);
        }
        int newPos;
        while(!indices.isEmpty()){
            for (LinguisticTree tree : bestTrees[indices.poll()]) {
                // do sth with tree
                out.println(tree.serializeLeafs());
                pseudoWords.add(tree.serializeLeafs());

                //out.flush();
                newPos = tree.getRightPosition() + 1;
                if(newPos < bestTrees.length)
                    indices.put(newPos);
            }
        }
        //System.out.println("finished");

        out.println("\nPSEUDOWORDS:");
        for(String pw: pseudoWords.sortByValue().keySet()){
            out.println(pseudoWords.get(pw)+"\t"+pw.replaceAll("\\\n", "\\n"));
        }
    }


    public void calculateTreePatternProbabilities(){
        //probabilities = new HashMap<>(treePatterns.size());
        int counter = 0;
        for(LinguisticTree tree: treePatterns.keySet()){
            tree.setProbability(tree.getPartitions().calculate(getTreePatterns()));
            counter++;
            //probabilities.put(tree,);
        }
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
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize().replaceAll("\\\n", "\\n") + "\t"+(probabilities.containsKey(treePart)?probabilities.get(treePart):"NULL"));
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: "+ probabilities.size());

    }

    public void printProbabilitiesSortedByValueAndKey(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        MultiSet<String> strings = new MultiSet<>(treePatterns.size());
        SortedSet<KeyValuePair<Double, String>> sortedSet = new TreeSet<>();
        for(LinguisticTree tree: treePatterns.keySet()){
            if(tree.isFull()) {
                //sortedSet.add(new KeyValuePair<>(probabilities.get(tree), tree));
                strings.add(tree.serialize().replaceAll("\\\n", "\\n"), tree.getProbability());
            }
        }

        for(String string: strings.keySet()){
            sortedSet.add(new KeyValuePair<>(strings.get(string), string));
        }
        for (KeyValuePair<Double, String> keyValuePair : sortedSet) {
            out.println(keyValuePair.key + "\t" + keyValuePair.value);
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getRelFrequ(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());
        //out.println("probabilities.size: " + probabilities.size());
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
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getRelFrequ(treePart, treePart));
        }*/
        out.println("treePatterns.size: " + treePatterns.size());

    }

    public void printTreePatternsSortedByKey(PrintStream out){
        out.println("print treePatterns (sortByKey)");
        for(LinguisticTree treePart: treePatterns.sortedKeySet()){
            out.println(treePart.serialize()+"\t"+treePatterns.get(treePart));
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

