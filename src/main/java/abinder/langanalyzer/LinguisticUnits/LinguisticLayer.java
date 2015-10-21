package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.*;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.MathUtils;

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
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees;
    ArrayList<HashMap<Integer, HashSet<LinguisticTree>>> previousTreesBySize;

    ArrayList<LinguisticTree>[] bestTrees;
    double[] bestProbabilities;
    MultiSet<Integer> sequenceProbs;
    //ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    // with set parents
    MultiSet<LinguisticTree> treePatterns;
    HashMap<LinguisticTree, Double> probabilities = new HashMap<>();
    private HashMap<LinguisticTree, Sum> partitions;

    int maxDepth = 3;
    int count = 0;
    int posOffset = 0;
    int processedTreesIndex = 0;
    String tabs = "";

    //just debugging
    public static long t1, t2, t3, t4;

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public LinguisticLayer(int maxDepth, int expectedSize){
        this.maxDepth = maxDepth;
        previousTrees = new ArrayList<>(expectedSize);
        previousTreesBySize = new ArrayList<>(expectedSize);
        sequenceProbs = new MultiSet<>(expectedSize);

        int maxTreeSize = 1 << maxDepth;
        long fullTreeCount = 1/(maxTreeSize + 1) * CombinatoricsUtils.binomialCoefficient(2 * maxTreeSize, maxTreeSize);
        // still not correct: misses subtrees. (sparse trees should be covered by "*2")
        long expectedPatternSize = fullTreeCount * 2 * (expectedSize-maxTreeSize+1);
        if(expectedPatternSize > Integer.MAX_VALUE)
            System.out.println("ERROR: expectedPatternSize = "+expectedPatternSize+" > Integer.MAX_VALUE = "+Integer.MAX_VALUE);
        treePatterns = new MultiSet<>((int) expectedPatternSize);
        treePatterns.add(new LinguisticTree(LinguisticType.TREE));
        // set position zero to 100%
        sequenceProbs.add(0);
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
        previousTrees.add(newTrees);


        // construct trees END

        count++;
    }


    public void updateTreePatterns(){
        long start1, start2;

        for(int endPos = processedTreesIndex-posOffset; endPos < previousTreesBySize.size(); endPos++){
            start1 = System.currentTimeMillis();
            //ArrayList<HashSet<LinguisticTree>> rearrangedTrees = new ArrayList<>();
            for(Integer size: previousTreesBySize.get(endPos).keySet()){
                //HashMap<Integer, Double> sizesRelFrequ = new HashMap<>(1<<(previousTrees.get(endPos).size()-1));
                double sizeRelFrequ = 0;
                for(LinguisticTree treeFixedPosSize: previousTreesBySize.get(endPos).get(size)) {

                    //for(LinguisticTree treeFixedSize: treesFixedPosition.get(size)) {
                        //rearrangedTrees.get(size).add(tree);
                        //probabilities.clear();
                        //tree.setParents(null);
                    double relFrequ = treeFixedPosSize.getPartitions().calculate(treePatterns);

                    sizeRelFrequ += relFrequ;
                    //allTreesProbs.add(size, prob);
                    treeFixedPosSize.setRelativeFrequency(relFrequ);
                        //addAllTreePattern(tree.calcPartitions().collectTerminals());
                    //}
                }
                //sizesRelFrequ.put(size, sizeRelFrequ);
                double newSequProb = sequenceProbs.get(processedTreesIndex + 1 - size)* sizeRelFrequ;
                sequenceProbs.add(processedTreesIndex + 1, newSequProb);//>=1.0?1.0:newSequProb);
            }
            t1+=System.currentTimeMillis()-start1;
            start2 = System.currentTimeMillis();
            int addPos = processedTreesIndex + 1 - (1<<(maxDepth-1));
            if(addPos >= 0){
                double probSum = 0;
                for(int size = 1; size <= (1<<(maxDepth-1)) && addPos >= size -1; size++){
                    for(int tempEndPos = addPos; tempEndPos < addPos + size; tempEndPos++){
                        double prevSequProb = sequenceProbs.get(tempEndPos - size +1);
                        for(LinguisticTree tree: previousTreesBySize.get(tempEndPos).get(size)) {
                            probSum += prevSequProb*tree.getRelativeFrequency();
                        }
                    }
                }

                for(int size = 1; size <= (1<<(maxDepth-1)) && addPos >= size -1; size++){
                    //for(int tempEndPos = addPos; tempEndPos < addPos + size; tempEndPos++){
                        double prevSequProb = sequenceProbs.get(addPos - size +1);
                        for(LinguisticTree tree: previousTreesBySize.get(addPos).get(size)) {
                            double currentProb = prevSequProb*tree.getRelativeFrequency() / probSum;
                            for(LinguisticTree part: tree.getPartitions().collectTerminals()) {
                                treePatterns.add(part, currentProb);
                            }
                        }
                    //}
                }

            }




            /*int size = 0;
            for(HashSet<LinguisticTree> trees: rearrangedTrees){
                double sequenceProb = sequenceProbs.get(processedTreesIndex - posOffset - size);
                sequenceProbs.add(processedTreesIndex-posOffset+1, allTreesProbs.get(size)*sequenceProb);
                for(LinguisticTree tree: trees){
                    for(LinguisticTree part: tree.getPartitions().collectTerminals()) {
                        treePatterns.add(part, sequenceProb * tree.getRelativeFrequency());
                    }
                }
                size++;
            }*/
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


    public void calcBestPaths(){
        //if(bestProbabilities == null)
            bestProbabilities = new double[previousTrees.size()+1]; //new ArrayList<>(previousTrees.size());
        //if(bestTrees==null)
            bestTrees = (ArrayList<LinguisticTree>[]) Array.newInstance(ArrayList.class, previousTrees.size());//(new ArrayList[previousTrees.size()]); //new ArrayList<>(previousTrees.size());


       for(int i=previousTrees.size()-1; i>=0; i--){
           for(ArrayList<LinguisticTree> currentTrees: previousTrees.get(i)){
               ArrayList<LinguisticTree>[] rearrangedTrees = (ArrayList<LinguisticTree>[]) Array.newInstance(ArrayList.class, (int)Math.pow(previousTrees.get(i).size(),2));

               for(LinguisticTree tree: currentTrees) {
                   int size = tree.getRightPosition() - tree.getLeftPosition();
                   if(rearrangedTrees[size] == null)
                       rearrangedTrees[size] = new ArrayList<>();
                   rearrangedTrees[size].add(tree);
               }
               for(ArrayList<LinguisticTree> trees: rearrangedTrees){
                    //ArrayList<LinguisticTree> trees = (ArrayList<LinguisticTree>)otrees;
                   if(trees==null)
                       continue;
                   LinguisticTree bestTree = null;
                   double bestProb = 0;
                   double sumProb = 0;
                   for(LinguisticTree tree: trees) {
                       double currentProb = tree.getPartitions().calculate(treePatterns);
                       sumProb += currentProb;
                       if(currentProb > bestProb){
                           bestProb = currentProb;
                           bestTree = tree;
                       }
                   }

                   double newProb = Math.log(bestProb) + bestProbabilities[i + 1];
                   int newPos = bestTree.getLeftPosition();
                   if (bestProbabilities[newPos] == 0.0 || newProb >= bestProbabilities[newPos]) {

                       if (bestTrees[newPos]==null)//newProb != bestProbabilities[newPos])
                           bestTrees[newPos] = new ArrayList<>();
                       bestProbabilities[newPos] = newProb;
                       bestTrees[newPos].add(bestTree);
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
        MultiSet<String> pseudoWords = new MultiSet<>(previousTrees.size());
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
        for(LinguisticTree tree: treePatterns.keySet()){
            //System.out.println(tree.serialize(false));
            //if(tree.serialize(false).equals("[[a,b],[c,d]]")) {
                //getRelFrequ(tree, tree, tree.getLeafCount()-1);
                //tree.setParents(null);
                //getProb(tree);

                probabilities.put(tree,tree.getPartitions().calculate(getTreePatterns()));
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

                    out.println(tree.serialize()+"\t"+tree.getPartitions().calculate(treePatterns));

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
                    System.out.println(tree.getPartitions().calculate(treePatterns));
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
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize().replaceAll("\\\n", "\\n") + "\t"+(probabilities.containsKey(treePart)?probabilities.get(treePart):"NULL"));
        }
        out.println("treePatterns.size: " + treePatterns.size());
        out.println("probabilities.size: "+probabilities.size());

    }

    public void printProbabilitiesSortedByValueAndKey(PrintStream out){
        out.println("print treePatterns (sortedByValueAndKey)");
        SortedSet<KeyValuePair<Double, LinguisticTree>> sortedSet = new TreeSet<>();
        for(LinguisticTree tree: treePatterns.keySet()){
                sortedSet.add(new KeyValuePair<>(treePatterns.getRelFrequ(tree), tree));
        }
        for (KeyValuePair<Double, LinguisticTree> keyValuePair : sortedSet) {
            out.println(keyValuePair.key + "\t" + keyValuePair.value.serialize().replaceAll("\\\n", "\\n"));
        }

        /*for(LinguisticTree treePart: treePatterns.sortByValue().keySet()){
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getRelFrequ(treePart, treePart));
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
            out.println(treePatterns.get(treePart)+"\t"+treePart.serialize(false) + "\t"+getRelFrequ(treePart, treePart));
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

