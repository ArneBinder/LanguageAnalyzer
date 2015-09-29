package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.IO;

import java.lang.*;
import java.util.*;

/**
 * Created by Arne on 17.09.2015.
 */
public class LinguisticTree implements Comparable<LinguisticTree>{
    private LinguisticTree leftChild;
    private LinguisticTree rightChild;
    private LinguisticTree parent;
    private LinguisticToken leaf;
    private ArrayList<LinguisticTree> parents;

    //caching
    private String serialization = null;
    private String serializationPL = null;
    private int depth = -1;
    private int minDepth = -1;
    private int leftPos = -1;
    private int rightPos = -1;
    private double probability = -1;
    private int leafCount = -1;

    private static final char charEscape = '\\';
    private static final char charOpen = '[';
    private static final char charClose = ']';
    private static final char charSeperate = ',';
    private static final char charNull = 'X';
    private static final HashSet<java.lang.Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charSeperate, charNull));

    private boolean defaultUsePositions = false;

    public LinguisticTree(LinguisticToken token) {
        leaf = token;
    }

    public LinguisticTree(String serialization) {
        deserialize(serialization);
    }

    public LinguisticTree() {
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild, boolean usePositions) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.defaultUsePositions = usePositions;
    }

    public LinguisticTree(LinguisticToken token, boolean usePositions) {
        leaf = token;
        this.defaultUsePositions = usePositions;
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild, LinguisticTree parent) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.parent = parent;
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public void setParents(LinguisticTree parent){
        this.parent = parent;
        if(leftChild!=null)
            leftChild.setParents(this);
        if(rightChild!=null)
            rightChild.setParents(this);
    }

    /*
    public LinguisticTree copyThisWithoutChild(LinguisticTree exceptChild){
        if(isLeaf())
            return new LinguisticTree(leaf);
        if(exceptChild.equals(leftChild)){
            if(rightChild!=null)
                return new LinguisticTree(null, rightChild.copyThis());
            else
                return null;
        }
        if(exceptChild.equals(rightChild)){
            if(leftChild!=null)
                return new LinguisticTree(leftChild.copyThis(), null);
            else
                return null;
        }
        return new LinguisticTree(leftChild!=null?leftChild.copyThisWithoutChild(exceptChild):null, rightChild!=null?rightChild.copyThisWithoutChild(exceptChild):null);
    }
*/
    public LinguisticTree copyThis(){
        LinguisticTree result;
        if(isLeaf())
            result = new LinguisticTree(leaf);
        else
            result = new LinguisticTree(leftChild!=null?leftChild.copyThis():null, rightChild!=null?rightChild.copyThis():null);
        result.setSerializationPL(this.getSerializationPL());
        return result;
    }


    public boolean isLeaf() {
        return (leftChild == null && rightChild == null);
    }

    public boolean isRoot(){
        return parent==null;
    }

    public LinguisticToken getLeaf() {
        return leaf;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public LinguisticTree getLeftChild() {
        return leftChild;
    }

    public LinguisticTree getRightChild() {
        return rightChild;
    }

    @Override
    public boolean equals(Object other) {
        return (other != null)
                &&(other instanceof LinguisticTree)
                && ((LinguisticTree) other).serialize(defaultUsePositions).equals(this.serialize(defaultUsePositions));
    }

    public boolean isFull(){
        if(isLeaf() && leaf!=null)
            return true;
        if(leftChild==null || rightChild==null)
            return false;
        return leftChild.isFull() && rightChild.isFull();
    }

    /*
    public boolean equals(Object other) {
        if(other == null || !(other instanceof LinguisticTree))
            return false;
        LinguisticTree oTree = (LinguisticTree) other;
        if(leaf != null){
            return oTree.getLeaf()!=null
                    && leaf.getType().equals(oTree.getLeaf().getType());
        }
        boolean result;
        if(leftChild!=null){
            result = leftChild.equals(oTree.leftChild);
        }else{
            result = oTree.leftChild == null;
        }
        if(rightChild!=null){
            result &= rightChild.equals(oTree.rightChild);
        }else{
            result &= oTree.rightChild == null;
        }
        return result;
    }
    */

    public String serializeToRoot(String childSerialization, LinguisticTree root) {
        if(this==root)
            return childSerialization;
        if(parent.leftChild!=null && parent.leftChild == this){
            return parent.serializeToRoot(charOpen + childSerialization + charSeperate + "" + charNull + "" + charClose, root);
        }else if(parent.rightChild!=null && parent.rightChild == this){
            return parent.serializeToRoot(charOpen + "" + charNull + "" + charSeperate + childSerialization + charClose, root);
        }else{
            System.out.println("ERROR: this ("+this.serialize(false)+") is neither leftChild nor RightChild of parent: "+parent.serialize(false));
            return null;
            //throw new Exception("this ("+this.serialize(false)+") is neither leftChild nor RightChild of parent: "+parent.serialize(false));
        }
    }

    public LinguisticTree copyToRoot(LinguisticTree childTree, LinguisticTree root) {
        if(this==root)
            return childTree;
        LinguisticTree newParent = new LinguisticTree();
        childTree.parent = newParent;
        if(parent.leftChild!=null && parent.leftChild == this){
            newParent.setLeftChild(childTree);
            return parent.copyToRoot(newParent, root);
        } else if (parent.rightChild != null && parent.rightChild == this){
            newParent.setRightChild(childTree);
            return parent.copyToRoot(newParent, root);
        } else {
            System.out.println("ERROR: this ("+this.serialize(false)+") is neither leftChild nor RightChild of parent: "+parent.serialize(false));
            return null;
            //throw new Exception("this ("+this.serialize(false)+") is neither leftChild nor RightChild of parent: "+parent.serialize(false));
        }
    }

    // TODO: fix this!
    public ArrayList<LinguisticTree> getTreeParts(LinguisticTree currentHead){
        ArrayList<LinguisticTree> result = new ArrayList<>();

        LinguisticTree currentRootTree = currentHead.copyToRoot(currentHead.copyThis(),this);
        result.add(currentRootTree);
        //System.out.println("\t"+currentRootTree.serialize(false));

        /*if(currentHead.getLeftChild()!=null && currentHead.getRightChild()!=null){
            result.addAll(currentHead.getLeftChild().getTreeParts(currentHead.getLeftChild()));
            result.addAll(currentHead.getRightChild().getTreeParts(currentHead.getRightChild()));
        }*/

        if(currentHead.getLeftChild()!=null){
            result.addAll(getTreeParts(currentHead.getLeftChild()));
        }

        if(currentHead.getRightChild()!=null){
            result.addAll(getTreeParts(currentHead.getRightChild()));
        }

        return result;
    }


    @Override
    public int hashCode() {
        return serialize(defaultUsePositions).hashCode();
    }

    public boolean equalsPositionDependent(LinguisticTree other) {
        return this.serialize(true).equals(other.serialize(true));
    }

    public void resetSerializations(){
        serialization = null;
        serializationPL = null;
        leafCount = -1;
        if(parent!=null){
            parent.resetSerializations();
        }
    }

    public void resetLeftPositions(int prevleftPosition){
        if(prevleftPosition == leftPos && leftPos != -1){
            if(leftChild!=null){
                leftPos = leftChild.getLeftPosition();
            }else if(rightChild!=null){
                leftPos = rightChild.getLeftPosition();
            }else
                leftPos = -1;
            if(parent!=null){
                parent.resetLeftPositions(prevleftPosition);
            }
        }
    }

    public void resetRightPositions(int prevRightPosition){
        if(prevRightPosition == rightPos && rightPos != -1){
            if(rightChild!=null){
                rightPos = rightChild.getRightPosition();
            }else if(leftChild != null){
                rightPos = leftChild.getRightPosition();
            }else
                rightPos = -1;
            if(parent!=null)
                parent.resetRightPositions(prevRightPosition);
        }
    }

    public LinguisticTree deleteLeftChild(){
        LinguisticTree left = leftChild;
        leftChild = null;
        resetSerializations();
        resetLeftPositions(leftPos);
        return left;
    }

    public LinguisticTree deleteRightChild(){
        LinguisticTree right = rightChild;
        rightChild = null;
        resetSerializations();
        resetRightPositions(rightPos);
        return right;
    }

    public void setLeftChild(LinguisticTree leftChild) {
        this.leftChild = leftChild;
        resetLeftPositions(leftPos);
        resetSerializations();

    }

    public void setRightChild(LinguisticTree rightChild) {
        this.rightChild = rightChild;
        resetRightPositions(rightPos);
        resetSerializations();
    }

    public int getLeafCount() {
        if(leafCount>=0)
            return leafCount;
        if(isLeaf()){
            if(leaf!=null)
                leafCount = 1;
            else
                leafCount = 0;
        }else{
            leafCount = 0;
            if(leftChild!=null)
                leafCount += leftChild.getLeafCount();
            if(rightChild!=null)
                leafCount+=rightChild.getLeafCount();
        }

        return leafCount;
    }

    public int getSize(){
        return getRightPosition() - getLeftPosition() + 1;
    }

    public String serialize(boolean showPosition) {
        if (showPosition) {
            if (serialization == null) {
                if (isLeaf())
                    serialization = IO.escape(leaf.serialize(showPosition), escapeAbleChars, charEscape);
                else {
                    serialization = charOpen + (leftChild != null ? leftChild.serialize(showPosition) : charNull + "") + charSeperate + (rightChild != null ? rightChild.serialize(showPosition) : charNull + "") + charClose;
                }
            }
            return serialization;
        } else {
            if(serializationPL == null){
                if (isLeaf())
                    serializationPL = IO.escape(leaf.serialize(showPosition), escapeAbleChars, charEscape);
                else {
                    serializationPL = charOpen + (leftChild != null ? leftChild.serialize(showPosition) : charNull + "") + charSeperate + (rightChild != null ? rightChild.serialize(showPosition) : charNull + "") + charClose;
                }
            }
            return serializationPL;
        }

    }

    public void deserialize(String serialization){
        if(serialization==null) {
            System.out.println("ERROR: serialization is empty!");
            return;
        }

        if(serialization.charAt(0)!=charOpen || serialization.charAt(serialization.length()-1)!=charClose){
            leftChild = null;
            rightChild = null;
            leaf = new LinguisticToken(serialization);
            return;
        }
        String s = serialization.substring(1,serialization.length()-1);
        int open = 0;
        for(int i=0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c==charEscape){
                i++;
            }else if(c==charOpen) {
                open++;
            }else if(c==charClose) {
                open--;
            }else if(c==charSeperate){
                if(open==0){
                    String left = s.substring(0,i);
                    String right = s.substring(i+1);
                    if(left.equals(charNull+"")){
                        leftChild = null;
                    }else{
                        leftChild = new LinguisticTree();
                        leftChild.deserialize(left);
                    }
                    if(right.equals(charNull+"")){
                        rightChild = null;
                    }else{
                        rightChild = new LinguisticTree();
                        rightChild.deserialize(right);
                    }
                    return;
                }
            }
        }


    }

    public String getSerializationPL() {
        return serializationPL;
    }

    public void setSerializationPL(String serializationPL) {
        this.serializationPL = serializationPL;
    }

    /**
     * Calculates the depth of the tree.
     * If leftChild and rightChild are null (this is a leaf), the depth is 0.
     * Otherwise it is the maximum depth of the children +1.
     *
     * @return the depth of the current tree
     */
    public int getDepth() {
        if(depth >= 0)
            return depth;
        if (isLeaf()) {
            depth = 0;
        } else {
            depth = Math.max(leftChild != null ? leftChild.getDepth() : 0, rightChild != null ? rightChild.getDepth() : 0) + 1;
        }
        return depth;
    }

    public int getMinDepth() {
        if(minDepth >= 0)
            return minDepth;
        if (isLeaf()) {
            minDepth = 0;
        } else {
            minDepth = Math.min(leftChild != null ? leftChild.getDepth() : 0, rightChild != null ? rightChild.getDepth() : 0) + 1;
        }
        return minDepth;
    }

    public int getLeftPosition(){
        if(leftPos>=0)
            return leftPos;
        if(isLeaf())
            leftPos = leaf.getPosition();
        else {
            if(leftChild!=null)
                leftPos = leftChild.getLeftPosition();
            else
                leftPos = rightChild.getLeftPosition();
        }
        return leftPos;
    }

    public int getRightPosition(){
        if(rightPos>=0)
            return rightPos;
        if(isLeaf())
            rightPos = leaf.getPosition();
        else {
            if(rightChild!=null)
                rightPos = rightChild.getRightPosition();
            else
                rightPos = leftChild.getRightPosition();
        }
        return rightPos;
    }

    public ArrayList<LinguisticTree> getAllSubtrees(int maxDepth) {
        ArrayList<LinguisticTree> result = new ArrayList<>();
        if (getDepth() <= maxDepth)
            result.add(this);
        if (!isLeaf()) {
            if (leftChild != null) {
                result.addAll(leftChild.getAllSubtrees(maxDepth));
            }
            if (rightChild != null) {
                result.addAll(rightChild.getAllSubtrees(maxDepth));
            }
        }
        return result;
    }

    public ArrayList<LinguisticTree> getAllCutTrees() {
        ArrayList<LinguisticTree> result = new ArrayList<>();

        if (leftChild != null) {
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(), Collections.singletonList(null), this));
        }
        if (rightChild != null) {
            result.addAll(combineTreeLists(Collections.singletonList(null), rightChild.getAllCutTrees(), this));
        }
        if (leftChild != null && rightChild != null)
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(), rightChild.getAllCutTrees(), this));
        else
            result.add(this);
        return result;
    }

    /*
    public ArrayList<LinguisticTree> getAllCutTrees(MultiSet<LinguisticTree> treeSet) {
        ArrayList<LinguisticTree> result = new ArrayList<>();

        if (leftChild != null) {
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(treeSet), Collections.singletonList(null), treeSet));
        }
        if (rightChild != null) {
            result.addAll(combineTreeLists(Collections.singletonList(null), rightChild.getAllCutTrees(treeSet), treeSet));
        }
        if (leftChild != null && rightChild != null)
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(treeSet), rightChild.getAllCutTrees(treeSet), treeSet));
        else
            result.add(this);
        return result;
    }
    */

    /*
    public double getProbability(MultiSet<LinguisticTree> treeSet, ArrayList<LinguisticTree> parents, LinguisticTree currentChild){

        if(probability >= 0) {
            System.out.println("\t"+this.serialize(false)+"\t"+probability);
            return probability;
        }


        //System.out.println("\t"+this.serialize(false));

        probability = 0;


        if(leftChild == null && rightChild == null){
            if(parents.size() == 0) {
                Integer ownCount = treeSet.get(this);
                if (ownCount != null) {
                    probability = ownCount / treeSet.getTotalCount();
                }else{
                    //smoothing?
                }
                return probability;
            }else{

            }
        }


        // cut tree
        if(leftChild!=null){
            LinguisticTree newTreeLeftEmpty = new LinguisticTree(null, rightChild);
            if(treeSet.containsKey(newTreeLeftEmpty)){
                newTreeLeftEmpty = treeSet.getKey(newTreeLeftEmpty);
            }
            probability += leftChild.getProbability(treeSet, null, null) * newTreeLeftEmpty.getProbability(treeSet, parent, parentChild);

        }
        if(rightChild!=null){
            LinguisticTree newTreeRightEmpty = new LinguisticTree(leftChild,null);
            if(treeSet.containsKey(newTreeRightEmpty)){
                newTreeRightEmpty = treeSet.getKey(newTreeRightEmpty);
            }
            probability += rightChild.getProbability(treeSet, null, null) * newTreeRightEmpty.getProbability(treeSet, parent, parentChild);
        }
        //System.out.println(probability);

        return probability;
    }
    */

    /*
    public ArrayList<LinguisticTree> getPartition(){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        result.add(this);

        if(leftChild!=null)
            result.addAll(leftChild.getPartition());
        if(rightChild!=null)
            result.addAll(rightChild.getPartition());


        return result;
    }*/


    /*
    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList, MultiSet<LinguisticTree> treeSet) {
        ArrayList<LinguisticTree> result = new ArrayList<>(leftList.size() * rightList.size());
        for (LinguisticTree left : leftList) {
            for (LinguisticTree right : rightList) {
                LinguisticTree newTree = new LinguisticTree(left, right);
                if(treeSet.containsKey(newTree))
                    newTree = treeSet.getKey(newTree);
                result.add(newTree);
            }
        }
        return result;
    }
    */

    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList, LinguisticTree parent) {
        ArrayList<LinguisticTree> result = new ArrayList<>(leftList.size() * rightList.size());
        for (LinguisticTree left : leftList) {
            for (LinguisticTree right : rightList) {
                result.add(new LinguisticTree(left, right, parent));
            }
        }
        return result;
    }

    /*
    // depricated
    public static ArrayList<LinguisticTree> constructTrees(List<LinguisticToken> tokens, int maxDepth) {
        ArrayList<LinguisticTree> result = new ArrayList<>();
        if (tokens.size() == 1) {
            result.add(new LinguisticTree(tokens.get(0)));
            return result;
        }

        for (int i = 1; i < tokens.size(); i++) {
            if (maxDepth <= 0) {
                result.addAll(constructTrees(tokens.subList(0, i), maxDepth));
                result.addAll(constructTrees(tokens.subList(i, tokens.size()), maxDepth));
            } else {
                result.addAll(combineTreeLists(constructTrees(tokens.subList(0, i), maxDepth - 1), constructTrees(tokens.subList(i, tokens.size()), maxDepth - 1)));
            }
        }

        return result;
    }

    // depricated
    public static ArrayList<LinguisticTree> constructTrees2(List<LinguisticToken> tokens, int maxDepth){

        int offset = tokens.get(0).getPosition();

        ArrayList<LinguisticTree> result = new ArrayList<>();
        List<List<LinguisticTree>> lefts = new ArrayList<>(tokens.size());
        List<List<LinguisticTree>> rights = new ArrayList<>(tokens.size());

        ArrayList<LinguisticTree> highestTrees = new ArrayList<>(tokens.size());
        for(LinguisticToken token: tokens){
            LinguisticTree current = new LinguisticTree(token);
            highestTrees.add(current);
            ArrayList<LinguisticTree> l = new ArrayList<>();
            ArrayList<LinguisticTree> r = new ArrayList<>();
            l.add(current);
            r.add(current);
            lefts.add(l);
            rights.add(r);
        }
        result.addAll(highestTrees);


        int leftPos;
        int rightPos;
        //ArrayList<LinguisticTree> newTrees = new ArrayList<>(highestTrees.size());
        for(int currentDepth = 0; currentDepth < maxDepth; currentDepth++) {
            ArrayList<LinguisticTree> newTrees = new ArrayList<>(highestTrees.size());
            for(LinguisticTree highestTree: highestTrees){
                leftPos = highestTree.getLeftPosition();
                rightPos = highestTree.getRightPosition();
                if(leftPos > 0){
                    for(LinguisticTree smallerLeftTree: lefts.get(leftPos - 1 - offset)){
                        LinguisticTree newTree = new LinguisticTree(smallerLeftTree, highestTree);
                        newTrees.add(newTree);
                    }
                }

                if(rightPos < tokens.size()-1){
                    for(LinguisticTree smallerRightTree: rights.get(rightPos + 1 - offset)){
                        if(smallerRightTree.getDepth() < currentDepth) {
                            LinguisticTree newTree = new LinguisticTree(highestTree, smallerRightTree);
                            newTrees.add(newTree);
                        }
                    }
                }

            }
            for(LinguisticTree newTree: newTrees){
                leftPos = newTree.getLeftPosition();
                rightPos = newTree.getRightPosition();
                lefts.get(rightPos).add(newTree);
                rights.get(leftPos).add(newTree);
            }
            result.addAll(newTrees);
            highestTrees = newTrees;
        }



        return result;

    }

*/
    @Override
    public int compareTo(LinguisticTree o) {
        if(o == null)
            return 1;

        if(this.isLeaf()){
            if(this.leaf == null) {
                return -1;
            }else{
                if(o.isLeaf())
                    return leaf.compareTo(o.getLeaf());
                else
                    return -1;
            }
        }

        if(o.isLeaf())
            return 1;

        if(leftChild==null){ // --> rightChild != null
            if(o.leftChild==null){ // --> o.rightChild != null
                return rightChild.compareTo(o.rightChild);
            }else{ //o.leftChild != null
                return -1;
            }
        }else{  // --> leftChild != null
            if(o.leftChild==null){ // --> o.rightChild != null
                return 1;
            }else{ // --> o.leftChild != null
                int comp = leftChild.compareTo(o.leftChild);
                if(comp!=0)
                    return comp;
                if(rightChild==null) {
                    if(o.rightChild == null)
                        return 0;
                    return -1;
                }
                return rightChild.compareTo(o.rightChild);
            }
        }

        //return 0;
    }
}
