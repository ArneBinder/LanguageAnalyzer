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
    HashMap<String, Double> probabilities2 = new HashMap<>();

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
                   double newProb = Math.log(getProb(tree))+bestProbabilities[i+1];
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
                out.println(tree.serialize(false));

                //out.flush();
                newPos = tree.getRightPosition() + 1;
                if(newPos < bestTrees.length)
                    indices.put(newPos);
            }
        }
        //System.out.println("finished");
    }

    private String fltn(ArrayList<String> summands){
        if(summands.isEmpty())
            return "";
        return (summands.size()>1?"(":"")+String.join(" + ", summands)+(summands.size()>1?")":"");
    }

    public double getProb(LinguisticTree tree){
        tabs += "\t";

        //System.out.println(tabs+tree.serialize(false)+"\tENTER");
        double result = 0.0;//= treePatterns.getProbability(tree);
        LinguisticTree workingTree = tree.copyThis();
        workingTree.setParents(null);

        //LinguisticTree leftTree = null;
        ArrayList<LinguisticTree> leftTrees = new ArrayList<>();
        LinguisticTree cutParent = null;
        boolean visitedRoot = false;
        for(LinguisticTree leaf: workingTree.getLeafs()){
            //System.out.println(tabs+"\t"+workingTree.serialize(false)+"\tLEAF\t"+leaf.serialize(false));
            double currentProb = getCutProb(workingTree, leaf, null) + treePatterns.getProbability(workingTree);
            for(LinguisticTree leftTree: leftTrees){
                currentProb *= getProb(leftTree);
            }

            result += currentProb;
            //System.out.println(tabs+"\t"+currentProb);

            if(!visitedRoot && cutParent!=null)
                cutParent.setLeftChild(leftTrees.remove(leftTrees.size()-1));

            cutParent = leaf.getMaxLeftTree().getParent();

            // is not last leaf?
            if(cutParent!=null) {
                if(cutParent==workingTree)
                    visitedRoot = true;
                leftTrees.add(cutParent.deleteLeftChild());
            }

        }

        //System.out.println(tabs+tree.serialize(false)+"\tRETURN\t"+result);
        probabilities.put(tree, result);
        tabs = tabs.substring(1);
        return result;
    }

    public double getCutProb(LinguisticTree tree, LinguisticTree currentPos, LinguisticTree lastPos){
        tabs += "\t";
        //System.out.println(tabs + tree.serialize(false) + "\tENTER\t" + currentPos.serialize(false) + "\tFROM\t" + (lastPos != null ?lastPos.serialize(false):"NULL"));
        double result = 0.0;
        LinguisticTree parent = currentPos.getParent();
        LinguisticTree left = currentPos.getLeftChild();
        LinguisticTree right = currentPos.getRightChild();
        if(lastPos==parent){
            if(right!=null){
                if(left!=null) {
                    right = currentPos.deleteRightChild();
                    //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> LEFTCHILD)");
                    result += getProb(right) * (treePatterns.getProbability(tree) + getCutProb(tree, left, currentPos));
                    currentPos.setRightChild(right);
                }
                result += getCutProb(tree, right, currentPos);
            }

            if(left!=null){
                if(right!=null) {
                    left = currentPos.deleteLeftChild();
                    //System.out.println(tabs + left.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> RIGHTCHILD)");
                    result += getProb(left) * (treePatterns.getProbability(tree) + getCutProb(tree, right, currentPos));
                    currentPos.setLeftChild(left);
                }
                result += getCutProb(tree, left, currentPos);

            }
        }else if(lastPos==left){
            if(right!=null){
                right = currentPos.deleteRightChild();
                //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> PARENT)");
                result += getProb(right) * (treePatterns.getProbability(tree)+ (parent!=null?getCutProb(tree, parent, currentPos):0.0));
                currentPos.setRightChild(right);
                result += getCutProb(tree, right, currentPos);
            }
            if(parent!=null){
                result += getCutProb(tree, parent, currentPos);
            }

        }else if(lastPos==right && parent!=null){
            result += getCutProb(tree, parent, currentPos);
        }

        //System.out.println(tabs+tree.serialize(false)+"\tCUT\t"+result);
        tabs = tabs.substring(1);
        return result;
    }


    public ArrayList<String> getPartitions(LinguisticTree tree){
        tabs += "\t";
        ArrayList<String> summands = new ArrayList<>();
        //System.out.println(tabs+tree.serialize(false)+"\tENTER");
        //double result = 0.0;//= treePatterns.getProbability(tree);
        LinguisticTree workingTree = tree.copyThis();
        workingTree.setParents(null);

        //LinguisticTree leftTree = null;
        ArrayList<LinguisticTree> leftTrees = new ArrayList<>();
        LinguisticTree cutParent = null;
        boolean visitedRoot = false;
        String summand;
        for(LinguisticTree leaf: workingTree.getLeafs()){
            //System.out.println(tabs+"\t"+workingTree.serialize(false)+"\tLEAF\t"+leaf.serialize(false));
            ArrayList<String> nl = getCutPartitions(workingTree, leaf, null);
            nl.add(workingTree.serialize(false));
            summand = "";
            for(LinguisticTree leftTree: leftTrees){
                ArrayList<String> nl2 = getPartitions(leftTree);
                summand += " o "+fltn(nl2);
            }

            if(summand.equals(""))
                summands.addAll(nl);
            else
                summands.add(fltn(nl) + summand);

            //result += currentProb;
            //System.out.println(tabs+"\t"+currentProb);

            if(!visitedRoot && cutParent!=null)
                cutParent.setLeftChild(leftTrees.remove(leftTrees.size()-1));

            cutParent = leaf.getMaxLeftTree().getParent();

            // is not last leaf?
            if(cutParent!=null) {
                if(cutParent==workingTree)
                    visitedRoot = true;
                leftTrees.add(cutParent.deleteLeftChild());
            }

        }

        //System.out.println(tabs+tree.serialize(false)+"\tRETURN\t"+result);
        tabs = tabs.substring(1);
        return summands;
    }

    public ArrayList<String> getCutPartitions(LinguisticTree tree, LinguisticTree currentPos, LinguisticTree lastPos){
        tabs += "\t";
        ArrayList<String> summands = new ArrayList<>();
        //System.out.println(tabs + tree.serialize(false) + "\tENTER\t" + currentPos.serialize(false) + "\tFROM\t" + (lastPos != null ?lastPos.serialize(false):"NULL"));
        //double result = 0.0;
        LinguisticTree parent = currentPos.getParent();
        LinguisticTree left = currentPos.getLeftChild();
        LinguisticTree right = currentPos.getRightChild();
        if(lastPos==parent){
            if(right!=null){
                if(left!=null) {
                    right = currentPos.deleteRightChild();
                    //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> LEFTCHILD)");
                    ArrayList<String> nl = getPartitions(right);
                    ArrayList<String> nl2 = getCutPartitions(tree, left, currentPos);
                    nl2.add(tree.serialize(false));
                    if(!nl.isEmpty())
                        summands.add(fltn(nl)+ " o " + fltn(nl2));
                    else
                        summands.addAll(nl2);
                    currentPos.setRightChild(right);
                }
                summands.addAll(getCutPartitions(tree, right, currentPos));
            }

            if(left!=null){
                if(right!=null) {
                    left = currentPos.deleteLeftChild();
                    //System.out.println(tabs + left.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> RIGHTCHILD)");
                    ArrayList<String> nl =  getPartitions(left);
                    ArrayList<String> nl2 = getCutPartitions(tree, right, currentPos);
                    nl2.add(tree.serialize(false));
                    if(!nl.isEmpty())
                        summands.add(fltn(nl)+ " o " + fltn(nl2));
                    else
                        summands.addAll(nl2);
                    currentPos.setLeftChild(left);
                }
                summands.addAll(getCutPartitions(tree, left, currentPos));
            }
        }else if(lastPos==left){
            if(right!=null){
                right = currentPos.deleteRightChild();
                //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> PARENT)");
                ArrayList<String> nl = getPartitions(right);
                ArrayList<String> nl2;
                if(parent!=null)
                    nl2 = getCutPartitions(tree, parent, currentPos);
                else
                    nl2 = new ArrayList<>();
                nl2.add(tree.serialize(false));
                if(!nl.isEmpty())
                    summands.add(fltn(nl)+ " o " + fltn(nl2));
                else
                    summands.addAll(nl2);
                currentPos.setRightChild(right);
                summands.addAll(getCutPartitions(tree, right, currentPos));
            }
            if(parent!=null){
                summands.addAll(getCutPartitions(tree, parent, currentPos));
            }

        }else if(lastPos==right && parent!=null){
            summands.addAll(getCutPartitions(tree, parent, currentPos));
        }

        //System.out.println(tabs+tree.serialize(false)+"\tCUT\t"+result);
        tabs = tabs.substring(1);
        return summands;
    }

    public Sum getOperations(LinguisticTree tree){
        tabs += "\t";
        Sum sum = new Sum();
        //System.out.println(tabs+tree.serialize(false)+"\tENTER");
        //double result = 0.0;//= treePatterns.getProbability(tree);
        LinguisticTree workingTree = tree.copyThis();
        workingTree.setParents(null);

        //LinguisticTree leftTree = null;
        ArrayList<LinguisticTree> leftTrees = new ArrayList<>();
        LinguisticTree cutParent = null;
        boolean visitedRoot = false;
        for(LinguisticTree leaf: workingTree.getLeafs()){
            //System.out.println(tabs+"\t"+workingTree.serialize(false)+"\tLEAF\t"+leaf.serialize(false));
            Operation currentSum = getCutOperations(workingTree, leaf, null);
            currentSum.addOperand(workingTree.copyThis());
            currentSum.flatten();
            Operation product = new Product();
            product.addOperand(currentSum);

            for(LinguisticTree leftTree: leftTrees){
                product.addOperand(getOperations(leftTree));
            }
            product.flatten();
            sum.addOperand(product);

            if(!visitedRoot && cutParent!=null)
                cutParent.setLeftChild(leftTrees.remove(leftTrees.size()-1));

            cutParent = leaf.getMaxLeftTree().getParent();

            // is not last leaf?
            if(cutParent!=null) {
                if(cutParent==workingTree)
                    visitedRoot = true;
                leftTrees.add(cutParent.deleteLeftChild());
            }

        }
        sum.flatten();

        //System.out.println(tabs+tree.serialize(false)+"\tRETURN\t"+result);
        tabs = tabs.substring(1);
        return sum;
    }

    public Sum getCutOperations(LinguisticTree tree, LinguisticTree currentPos, LinguisticTree lastPos){
        tabs += "\t";
        Sum sum = new Sum();
        //System.out.println(tabs + tree.serialize(false) + "\tENTER\t" + currentPos.serialize(false) + "\tFROM\t" + (lastPos != null ?lastPos.serialize(false):"NULL"));
        //double result = 0.0;
        LinguisticTree parent = currentPos.getParent();
        LinguisticTree left = currentPos.getLeftChild();
        LinguisticTree right = currentPos.getRightChild();
        if(lastPos==parent){
            if(right!=null){
                if(left!=null) {
                    right = currentPos.deleteRightChild();
                    //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> LEFTCHILD)");
                    Operation rightBranch = getOperations(right);
                    Operation leftRemaining = getCutOperations(tree, left, currentPos);
                    leftRemaining.addOperand(tree.copyThis());
                    Operation product = new Product();
                    product.addOperand(rightBranch);
                    product.addOperand(leftRemaining);
                    product.flatten();
                    sum.addOperand(product);
                    currentPos.setRightChild(right);
                }
                sum.addOperand(getCutOperations(tree, right, currentPos));
            }

            if(left!=null){
                if(right!=null) {
                    left = currentPos.deleteLeftChild();
                    //System.out.println(tabs + left.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> RIGHTCHILD)");
                    Operation leftBranch =  getOperations(left);
                    Operation rightRemaining = getCutOperations(tree, right, currentPos);
                    rightRemaining.addOperand(tree.copyThis());
                    Operation product = new Product();
                    product.addOperand(leftBranch);
                    product.addOperand(rightRemaining);
                    product.flatten();
                    sum.addOperand(product);
                    currentPos.setLeftChild(left);
                }
                sum.addOperand(getCutOperations(tree, left, currentPos));
            }
        }else if(lastPos==left){
            if(right!=null){
                right = currentPos.deleteRightChild();
                //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> PARENT)");
                Operation rightBranch = getOperations(right);
                Operation leftRemaining;
                if(parent!=null)
                    leftRemaining = getCutOperations(tree, parent, currentPos);
                else
                    leftRemaining = new Sum();
                leftRemaining.addOperand(tree.copyThis());
                Operation product = new Product();
                product.addOperand(rightBranch);
                product.addOperand(leftRemaining);
                product.flatten();
                sum.addOperand(product);
                currentPos.setRightChild(right);
                sum.addOperand(getCutOperations(tree, right, currentPos));
            }
            if(parent!=null){
                sum.addOperand(getCutOperations(tree, parent, currentPos));
            }

        }else if(lastPos==right && parent!=null){
            sum.addOperand(getCutOperations(tree, parent, currentPos));
        }

        //System.out.println(tabs+tree.serialize(false)+"\tCUT\t"+result);
        tabs = tabs.substring(1);
        sum.flatten();
        return sum;
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

                    out.println(tree.serialize(false)+"\t"+getProb(tree));

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

                    out.println(tree.serialize(false));
                    System.out.println(getProb(tree));
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
            //if(!entry.getKey().isFull())
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

    public void printTreePatternsSortedByValue(PrintStream out){
        out.println("print treePatterns (sortByValue)");
        for(LinguisticTree treePart: MultiSet.sortByValue(treePatterns).keySet()){
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

