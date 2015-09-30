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

    boolean print = false;
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


    public void updateTreePatterns(PrintStream out){
        double threshold = 1.5E-3;

        for(int i=processedTreesIndex-posOffset; i < previousTrees.size(); i++){
            for(ArrayList<LinguisticTree> trees: previousTrees.get(i)){
                for(LinguisticTree tree: trees) {
                    probabilities.clear();
                    tree.setParents(null);
                    double probability = getProbabilityForHead(tree, tree, new LinguisticTree[0]);
                    //if(treePatterns.size() < 20000 ||  probability > threshold) {
                        out.println(probability + "\t" + tree.serialize(false) + "\t" + treePatterns.getTotalCount()+"\t"+treePatterns.size());

                        addAllTreePattern(tree.getAllCutTrees());
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

    private String fltn(ArrayList<String> summands){
        if(summands.isEmpty())
            return "";
        return (summands.size()>1?"(":"")+String.join(" + ", summands)+(summands.size()>1?")":"");
    }

    public double getProb(LinguisticTree tree, List<String> summands){
        tabs += "\t";

        //System.out.println(tabs+tree.serialize(false)+"\tENTER");
        double result = 0.0;//= treePatterns.getProbability(tree);
        LinguisticTree workingTree = tree.copyThis();
        workingTree.setParents(null);

        //LinguisticTree leftTree = null;
        ArrayList<LinguisticTree> leftTrees = new ArrayList<>();
        LinguisticTree cutParent = null;
        boolean visitedRoot = false;
        String summand;
        for(LinguisticTree leaf: workingTree.getLeafs()){
            //System.out.println(tabs+"\t"+workingTree.serialize(false)+"\tLEAF\t"+leaf.serialize(false));
            ArrayList<String> nl = new ArrayList<>();
            double currentProb = getCutProb(workingTree, leaf, null, nl) + treePatterns.getProbability(workingTree);
            nl.add(workingTree.serialize(false));
            summand = "";
            for(LinguisticTree leftTree: leftTrees){
                ArrayList<String> nl2 = new ArrayList<>();
                currentProb *= getProb(leftTree, nl2);
                summand += " o "+fltn(nl2);
            }

            if(summand.equals(""))
                summands.addAll(nl);
            else
                summands.add(fltn(nl) + summand);

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
        tabs = tabs.substring(1);
        return result;
    }

    public double getCutProb(LinguisticTree tree, LinguisticTree currentPos, LinguisticTree lastPos, List<String> summands){
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
                    ArrayList<String> nl = new ArrayList<>();
                    ArrayList<String> nl2 = new ArrayList<>();
                    result += getProb(right, nl) * (treePatterns.getProbability(tree) + getCutProb(tree, left, currentPos, nl2));
                    nl2.add(tree.serialize(false));
                    if(!nl.isEmpty())
                        summands.add(fltn(nl)+ " o " + fltn(nl2));
                    else
                        summands.addAll(nl2);
                    currentPos.setRightChild(right);
                }
                ArrayList<String> nl3 = new ArrayList<>();
                result += getCutProb(tree, right, currentPos, nl3);
                summands.addAll(nl3);
            }

            if(left!=null){
                if(right!=null) {
                    left = currentPos.deleteLeftChild();
                    //System.out.println(tabs + left.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> RIGHTCHILD)");
                    ArrayList<String> nl = new ArrayList<>();
                    ArrayList<String> nl2 = new ArrayList<>();
                    result += getProb(left, nl) * (treePatterns.getProbability(tree) + getCutProb(tree, right, currentPos, nl2));
                    nl2.add(tree.serialize(false));
                    if(!nl.isEmpty())
                        summands.add(fltn(nl)+ " o " + fltn(nl2));
                    else
                        summands.addAll(nl2);
                    currentPos.setLeftChild(left);
                }
                ArrayList<String> nl3 = new ArrayList<>();
                result += getCutProb(tree, left, currentPos, nl3);
                summands.addAll(nl3);
            }
        }else if(lastPos==left){
            if(right!=null){
                right = currentPos.deleteRightChild();
                //System.out.println(tabs + right.serialize(false) + " o (" + tree.serialize(false) + " + " + currentPos.serialize(false) + " -> PARENT)");
                ArrayList<String> nl = new ArrayList<>();
                ArrayList<String> nl2 = new ArrayList<>();
                result += getProb(right, nl) * (treePatterns.getProbability(tree)+ (parent!=null?getCutProb(tree, parent, currentPos, nl2):0.0));
                nl2.add(tree.serialize(false));
                if(!nl.isEmpty())
                    summands.add(fltn(nl)+ " o " + fltn(nl2));
                else
                    summands.addAll(nl2);
                currentPos.setRightChild(right);
                ArrayList<String> nl3 = new ArrayList<>();
                result += getCutProb(tree, right, currentPos, nl3);
                summands.addAll(nl3);
            }
            if(parent!=null){
                ArrayList<String> nl3 = new ArrayList<>();
                result += getCutProb(tree, parent, currentPos, nl3);
                summands.addAll(nl3);
            }

        }else if(lastPos==right && parent!=null){
            ArrayList<String> nl3 = new ArrayList<>();
            result += getCutProb(tree, parent, currentPos, nl3);
            summands.addAll(nl3);
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

    public double getProbabilityForHead(LinguisticTree tree, LinguisticTree currentHead, LinguisticTree[] remainingTreeParts){
        //tabs += "\t";

        double result = 0;
        double currentProb = 0;
        String blindedSerialization = currentHead.serializeToRoot(currentHead.serialize(false), tree);
        LinguisticTree currentHeadTreeDummy = new LinguisticTree();
        currentHeadTreeDummy.setSerializationPL(blindedSerialization);
        //if(blindedSerialization.equals("[X,[[X,+],X]]"))
        //    System.out.println("BLA");
        if(probabilities.containsKey(currentHeadTreeDummy)){
            //System.out.println(tabs+blindedSerialization+"\tCACHED\t"+probabilities.get(currentHeadTreeDummy));
            //tabs = tabs.substring(1);
            return probabilities.get(currentHeadTreeDummy);
        }

        /*String partitionStr = blindedSerialization;
        for(LinguisticTree remainingTree: remainingTreeParts){
            partitionStr+=" o " + remainingTree.serialize(false);
        }
        System.out.println(tabs+partitionStr);
*/
        currentProb = treePatterns.getProbability(currentHeadTreeDummy);
        result = currentProb;


        for(LinguisticTree remainingTree: remainingTreeParts){
            result *= getProbabilityForHead(remainingTree, remainingTree, new LinguisticTree[0]);
        }

        if(currentHead.getLeftChild()!=null){
            if(currentHead.getRightChild()!=null) {
                LinguisticTree[] newRemainingTreeParts = new LinguisticTree[remainingTreeParts.length + 1];
                System.arraycopy(remainingTreeParts, 0, newRemainingTreeParts, 0, remainingTreeParts.length);
                newRemainingTreeParts[remainingTreeParts.length] = currentHead.getRightChild();
                result += getProbabilityForHead(tree, currentHead.getLeftChild(), newRemainingTreeParts);
            }else{
                result += getProbabilityForHead(tree, currentHead.getLeftChild(), remainingTreeParts);
            }
        }

        if(currentHead.getRightChild()!=null){
            if(currentHead.getLeftChild()!=null) {
                LinguisticTree[] newRemainingTreeParts = new LinguisticTree[remainingTreeParts.length + 1];
                System.arraycopy(remainingTreeParts, 0, newRemainingTreeParts, 0, remainingTreeParts.length);
                newRemainingTreeParts[remainingTreeParts.length] = currentHead.getLeftChild();
                result += getProbabilityForHead(tree, currentHead.getRightChild(),newRemainingTreeParts);
            }else{
                result += getProbabilityForHead(tree, currentHead.getRightChild(), remainingTreeParts);
            }
        }

        //System.out.println(tabs + partitionStr + "\tRETURN\t" + result);
        //tabs = tabs.substring(1);

        probabilities.put(new LinguisticTree(blindedSerialization), result);
        return result;
    }

    //DEPRECATED
    public double getProbability(LinguisticTree tree, LinguisticTree currentPos, int cutCount) {
        tabs += "\t";
        System.out.println(tabs + tree.serialize(false) + "\t"+ currentPos.serialize(false) + "\tVISIT \tcutCount: "+cutCount);

        double probability = 0;

        double currentProb = 0;

        //if (tree.serialize(false).equals("[¹,³]")) {
         //   System.out.println("HERE_A: " + probabilities.get(tree));
            //print = true;
        //}
        if (tree.serialize(false).equals("[[²,X],[¹,³]]")) {
            System.out.println("HERE");
            //print = true;
        }

        if(cutCount==0) {
            // TODO: fix caching!!
            if (tree.equals(currentPos)) {


                //String s = tree.serialize(false);
                if (probabilities.containsKey(tree)) {
                    //if(print) {
                       System.out.println(tabs + tree.serialize(false) + "\t" + currentPos.serialize(false) + "\tCACHED\t" + probabilities.get(tree));
                       tabs = tabs.substring(1);
                    //}
                    currentProb = probabilities.get(tree);
                    return currentProb;
                } else {
                    currentProb = treePatterns.getProbability(tree);
                    probability += currentProb;
                }
            }
        }

        if(cutCount > 0) {
            LinguisticTree leftChild = currentPos.getLeftChild();
            LinguisticTree rightChild = currentPos.getRightChild();


            // cut tree
            if (leftChild != null && rightChild != null) {
                // cut left child
                leftChild = currentPos.deleteLeftChild();
                //if(print)
                    System.out.println(tabs+leftChild.serialize(false)+ "\to\t"+  tree.serialize(false) + "\tCUT left\t");

                int cutCountRangeMin = cutCount-1-tree.getLeafCount()+1;
                int cutCountRangeMax = leftChild.getLeafCount()-1;
                for(int cutCounta = Math.max(0,cutCountRangeMin); cutCounta < Math.min(cutCount, cutCountRangeMax+1); cutCounta++) {
                        currentProb = getProbability(leftChild, leftChild, cutCounta) * getProbability(tree, tree, (cutCount - 1) - cutCounta);
                        probability += currentProb;

                }
                currentPos.setLeftChild(leftChild);

                // cut right child
                rightChild = currentPos.deleteRightChild();
                //if(print)
                    System.out.println(tabs+tree.serialize(false)+ "\to\t"+ rightChild.serialize(false)+"\tCUT right\t");
                cutCountRangeMin = cutCount-1-tree.getLeafCount()+1;
                cutCountRangeMax = rightChild.getLeafCount()-1;
                for(int cutCounta = Math.max(0,cutCountRangeMin); cutCounta < Math.min(cutCount, cutCountRangeMax+1); cutCounta++) {
                    currentProb = getProbability(rightChild, rightChild, cutCounta) * getProbability(tree, tree, (cutCount - 1) - cutCounta);
                    probability += currentProb;
                }
                currentPos.setRightChild(rightChild);
            }

            if (leftChild != null && leftChild.getSize() > 1) {
                //if(print)
                System.out.println(tabs + tree.serialize(false) + "\t"+ leftChild.serialize(false) + "\tLEFTCHILD");
                currentProb = getProbability(tree, leftChild, cutCount);
                probability += currentProb;
            }

            if (rightChild != null && rightChild.getSize() > 1) {
                //if(print)
                System.out.println(tabs + tree.serialize(false) + "\t"+ rightChild.serialize(false) + "\tRIGHTCHILD");
                currentProb = getProbability(tree, rightChild, cutCount);
                probability += currentProb;
            }
        }

        //if(print) {
            System.out.println(tabs + tree.serialize(false) + "\t" + currentPos.serialize(false) + "\tRETURN\t" + probability);
            tabs = tabs.substring(1);
        //}

        //if(probability > 0)
        if(tree.equals(currentPos) && cutCount==0) {
            if(tree.serialize(false).equals("[¹,³]"))
                System.out.println("HERE_B: "+probability);
            probabilities.put(tree.copyThis(), probability);
            //if(tree.serialize(false).equals("[[²,X],[¹,³]]")) {
            //    System.out.println("HEREOUT");
            //    print = false;
            //}
        }
        //else {
            //if(probability > 0)
            //    System.out.println("BLAAAA");
        //}
        return probability;
    }

    public void calculateTreePatternProbabilities(){
        for(LinguisticTree tree: treePatterns.keySet()){
            //System.out.println(tree.serialize(false));
            //if(tree.serialize(false).equals("[[a,b],[c,d]]")) {
                //getProbability(tree, tree, tree.getLeafCount()-1);
                tree.setParents(null);
                getProbabilityForHead(tree, tree, new LinguisticTree[0]);
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

                    out.println(tree.serialize(false)+"\t"+getProbability(tree, tree, tree.getLeafCount()-1));

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
                    System.out.println(getProb(tree, new ArrayList<String>()));
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
