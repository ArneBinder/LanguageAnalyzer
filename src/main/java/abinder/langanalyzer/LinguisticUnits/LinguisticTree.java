package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.*;

import java.lang.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
    private static final char charNull = '_';
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
        if(noChildren())
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
        if(noChildren())
            result = new LinguisticTree(leaf);
        else
            result = new LinguisticTree(leftChild!=null?leftChild.copyThis():null, rightChild!=null?rightChild.copyThis():null);
        result.setSerializationPL(this.getSerializationPL());
        return result;
    }


    public boolean noChildren() {
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

    public LinguisticTree getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object other) {
        return (other != null)
                &&(other instanceof LinguisticTree)
                && ((LinguisticTree) other).serialize(defaultUsePositions).equals(this.serialize(defaultUsePositions));
    }

    public boolean isFull(){
        if(noChildren() && leaf!=null)
            return true;
        if(leftChild==null || rightChild==null)
            return false;
        return leftChild.isFull() && rightChild.isFull();
    }

    public LinkedBlockingQueue<LinguisticTree> getLeafs(){
        LinkedBlockingQueue<LinguisticTree> result = new LinkedBlockingQueue<>();
        if(leaf!=null) {
            result.add(this);
            return result;
        }
        if(leftChild!=null){
            result.addAll(leftChild.getLeafs());
        }
        /*if(leftChild== null || rightChild==null){
            result.add(this);
        }*/
        if(rightChild!=null){
            result.addAll(rightChild.getLeafs());
        }
        return result;
    }


    public LinguisticTree getMaxLeftTree(){
        if(parent==null || (parent.getLeftChild()==this && parent.getRightChild()!=null))
            return this;
        return parent.getMaxLeftTree();
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
        leftChild.parent = this;
    }

    public void setRightChild(LinguisticTree rightChild) {
        this.rightChild = rightChild;
        resetRightPositions(rightPos);
        resetSerializations();
        rightChild.parent = this;
    }

    public int getLeafCount() {
        if(leafCount>=0)
            return leafCount;
        if(noChildren()){
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
                if (leaf!=null)
                    serialization = IO.escape(leaf.serialize(showPosition), escapeAbleChars, charEscape)+charOpen+charClose;
                else {
                    serialization = charNull+""+charOpen + (leftChild != null ? leftChild.serialize(showPosition) : charNull + "") + charSeperate + (rightChild != null ? rightChild.serialize(showPosition) : charNull + "") + charClose;
                }
            }
            return serialization;
        } else {
            if(serializationPL == null){
                if(leaf!=null) {
                    serializationPL = IO.escape(leaf.serialize(showPosition), escapeAbleChars, charEscape)+charOpen+""+charNull+"" + charSeperate + "" +charNull+""+charClose;
                }
                else {
                    serializationPL = charNull+""+charOpen + (leftChild != null ? leftChild.serialize(showPosition) : charNull + "") + charSeperate + (rightChild != null ? rightChild.serialize(showPosition) : charNull + "") + charClose;
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
        if(serialization.equals(""))
            return;

        if(serialization.charAt(0)!=charOpen || serialization.charAt(serialization.length()-1)!=charClose){
            leftChild = null;
            rightChild = null;
            if(serialization.charAt(0)!=charNull)
                leaf = new LinguisticToken(serialization);
            else
                leaf = null;
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
                    leftChild = new LinguisticTree();
                    if(!left.equals(charNull+"")){
                        leftChild.deserialize(left);
                    }
                    rightChild = new LinguisticTree();
                    if(!right.equals(charNull+"")){

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
        if (noChildren()) {
            depth = 0;
        } else {
            depth = Math.max(leftChild != null ? leftChild.getDepth() : 0, rightChild != null ? rightChild.getDepth() : 0) + 1;
        }
        return depth;
    }

    public void resetDepth(){
        depth = -1;
    }

    public int getMinDepth() {
        if(minDepth >= 0)
            return minDepth;
        if (noChildren()) {
            minDepth = 0;
        } else {
            minDepth = Math.min(leftChild != null ? leftChild.getDepth() : 0, rightChild != null ? rightChild.getDepth() : 0) + 1;
        }
        return minDepth;
    }

    public int getLeftPosition(){
        if(leftPos>=0)
            return leftPos;
        if(noChildren())
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
        if(noChildren())
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
        if (getDepth() <= maxDepth || maxDepth < 0)
            result.add(this);
        if (!noChildren()) {
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

        if (leftChild == null && rightChild!=null)
            result.addAll(combineTreeLists(Collections.singletonList(null), rightChild.getAllCutTrees(), this));
        if (rightChild == null && leftChild!=null)
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(), Collections.singletonList(null), this));
        if (leftChild != null && rightChild != null)
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(), rightChild.getAllCutTrees(), this));

        if(leftChild == null && rightChild==null && leaf!=null) {
            result.add(this);
        }
        result.add(new LinguisticTree(""));

        return result;
    }

    public int getEdgeCount(){
        if(leaf!=null || (leftChild==null && rightChild==null))
            return 0;
        int result = 0;
        if(leftChild!=null)
            result += 1+leftChild.getEdgeCount();
        if(rightChild!=null)
            result += 1+rightChild.getEdgeCount();
        return result;
    }

    public ArrayList<LinguisticTree> getNodes(){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        result.add(this);
        if(leaf!=null)
            return result;

        if(leftChild!=null)
            result.addAll(leftChild.getNodes());
        if(rightChild!=null)
            result.addAll(rightChild.getNodes());
        return result;
    }

    public Sum calcPartitions(){
        Sum result = new Sum();
        ArrayList<LinguisticTree> nodesList = getNodes();
        LinguisticTree[] nodes = new LinguisticTree[nodesList.size()];
        nodes = nodesList.toArray(nodes);

        LinguisticTree[] nodeBackups = new LinguisticTree[nodes.length];
        int[] leftPositions = new int[nodes.length];
        int[] rightPositions = new int[nodes.length];

        for(int i=0; i<nodes.length;i++){
            LinguisticTree tree = nodes[i];
            nodeBackups[i] = tree.copyThis();
            LinguisticTree left = tree.getLeftChild();
            if(left!=null) {
                for (int j = i + 1; j < nodes.length; j++) {
                    if(left==nodes[j])
                        leftPositions[i] = j;
                }
            }

            LinguisticTree right = tree.getRightChild();
            if(right!=null) {
                for (int j = i + 1; j < nodes.length; j++) {
                    if (right == nodes[j])
                        rightPositions[i] = j;
                }
            }
        }

        do{
            // construct partition
            Product current = constructPartition(0, nodes, nodeBackups, leftPositions, rightPositions ,true);
            result.addOperand(current);
        }while(incCut(nodes, nodeBackups, leftPositions, rightPositions));

        return result;

    }


    private Product constructPartition(int pos, LinguisticTree[] nodes, LinguisticTree[] nodeBackups, int[] leftPositions, int[] rightPositions, boolean add){
        Product result = new Product();
        LinguisticTree node = nodes[pos];
        LinguisticTree nodeBackup = nodeBackups[pos];
        if(add)
            result.addOperand(node.copyThis());
        // add left
        if(leftPositions[pos]>0){
            Product product = constructPartition(leftPositions[pos],nodes, nodeBackups,leftPositions, rightPositions, node.getLeftChild()==null && nodeBackup.getLeftChild()!=null);
            result.addAllTerminals(product.getTerminals());
        }

        // add right
        if(rightPositions[pos]>0) {
            Product product = constructPartition(rightPositions[pos], nodes, nodeBackups, leftPositions, rightPositions,node.getRightChild()==null && nodeBackup.getRightChild()!=null);
            result.addAllTerminals(product.getTerminals());
        }

        return result;
    }

    private boolean incCut(LinguisticTree[] nodes, LinguisticTree[] nodeBackups, int[] leftPositions, int[] rightPositions){
        for(int i=0; i<nodes.length; i++){
            LinguisticTree nodeBackup = nodeBackups[i];
            LinguisticTree node = nodes[i];
            if(nodeBackup.getLeaf()!=null)
                continue;
            if(nodeBackup.getLeftChild()!=null){
                if(node.getLeftChild()==null){
                    //remove cut
                    node.setLeftChild(nodes[leftPositions[i]]);
                }else{
                    nodes[leftPositions[i]] = node.deleteLeftChild();
                    return true;
                }
            }
            if(nodeBackup.getRightChild()!=null){
                if(node.getRightChild()==null){
                    //remove cut
                    node.setRightChild(nodes[rightPositions[i]]);
                }else{
                    nodes[rightPositions[i]] = node.deleteRightChild();
                    return true;
                }
            }
        }
        return false;
    }

    public double getCosineSimilarity(LinguisticTree other){
        MultiSet<LinguisticTree> thisTrees = new MultiSet<>();
        for(LinguisticTree subTree: getAllSubtrees(-1)){
            for(LinguisticTree cutTree: subTree.getAllCutTrees()){
                thisTrees.add(cutTree.copyThis());
            }
        }
        //this.getAllSubtrees(-1).forEach(element -> getAllCutTrees().forEach(thisTrees::add));

        MultiSet<LinguisticTree> otherTrees = new MultiSet<>();
        for(LinguisticTree subTree: other.getAllSubtrees(-1)){
            for(LinguisticTree cutTree: subTree.getAllCutTrees()){
                otherTrees.add(cutTree.copyThis());
            }
        }
        //other.getAllSubtrees(-1).forEach(element -> getAllCutTrees().forEach(otherTrees::add));
        return thisTrees.calcCosineSimilarity(otherTrees);
    }

    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList, LinguisticTree parent) {
        ArrayList<LinguisticTree> result = new ArrayList<>(leftList.size() * rightList.size());
        for (LinguisticTree left : leftList) {
            for (LinguisticTree right : rightList) {
                result.add(new LinguisticTree(left, right, parent));
            }
        }
        return result;
    }




    @Override
    public int compareTo(LinguisticTree o) {
        if(o == null)
            return 1;

        if(o.getDepth()!=this.getDepth()){
            if(this.getDepth() < o.getDepth())
                return -1;
            else
                return 1;
        }

        if(this.noChildren()){
            if(this.leaf == null) {
                return -1;
            }else{
                if(o.noChildren())
                    return leaf.compareTo(o.getLeaf());
                else
                    return -1;
            }
        }

        if(o.noChildren())
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

    public String toString(){
        return this.serialize(false);
    }
}
