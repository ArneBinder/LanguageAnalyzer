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
    //private ArrayList<LinguisticTree> parents;
    private LinguisticType label;

    //caching
    private String serializationPL = null;
    private int height = -1;
    private int minHeight = -1;
    private int leftPos = -1;
    private int rightPos = -1;
    private double probability = -1;
    private int leafCount = -1;
    private Disjunction<LinguisticTree> partitions = null;

    public static long t1, t2, t3, t4, t5, t6, t7, t8, t9, t10;
    public static int c1;

    private static final char charEscape = '\\';
    private static final char charOpen = '[';
    private static final char charClose = ']';
    private static final char charSeperate = ',';
    private static final char charNull = 'X';
    private static final HashSet<java.lang.Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charSeperate, charNull, '\n'));

    //private boolean defaultUsePositions = false;

    public LinguisticTree(LinguisticToken token, LinguisticType label) {
        leaf = token;
        this.label = label;
    }

    public LinguisticTree(String serialization, LinguisticType label) {
        deserialize(serialization);
        this.label = label;
    }

    //public LinguisticTree() {
    //}

    public LinguisticTree(LinguisticType label) {
        this.label = label;
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild, LinguisticType label) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.label = label;

        //this.defaultUsePositions = usePositions;
    }

    public LinguisticType getLabel() {
        return label;
    }

    public boolean isEmptyLeaf(){
        return leftChild == null && rightChild == null && leaf==null;
    }

    public void setParents(LinguisticTree parent){
        this.parent = parent;
        if(leftChild!=null)
            leftChild.setParents(this);
        if(rightChild!=null)
            rightChild.setParents(this);
    }

    public LinguisticTree copyThis(){
        LinguisticTree result;
        if(noChildren())
            result = new LinguisticTree(leaf, this.getLabel());
        else
            result = new LinguisticTree(leftChild!=null?leftChild.copyThis():null, rightChild!=null?rightChild.copyThis():null, label);
        result.setSerializationPL(this.getSerializationPL());
        // TODO: copy partitions
        return result;
    }


    public boolean noChildren() {
        return (leftChild == null && rightChild == null);
    }

    public boolean isRoot(){
        return parent==null;
    }

    public boolean isLeaf() {return leaf!=null;}

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

    public void setParent(LinguisticTree parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object other) {
        return (other != null)
                &&(other instanceof LinguisticTree)
                && ((LinguisticTree) other).serialize().equals(this.serialize());
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
        if(noChildren() || leaf!=null) {
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

    public String serializeLeafs(){
        String result = "";
        for(LinguisticTree tree: getLeafs()){
            result += tree.serialize();
        }
        return result;
    }


    public LinguisticTree getMaxLeftTree(){
        if(parent==null || (parent.getLeftChild()==this && parent.getRightChild()!=null))
            return this;
        return parent.getMaxLeftTree();
    }

    public Disjunction<LinguisticTree> getPartitions(ReconnectedMultiTreeSet treeParts){
        if(partitions==null)
           calcPartitions(treeParts);
        return partitions;
    }

    public Disjunction<LinguisticTree> getPartitions(){
        return partitions;
    }

    /*
    public boolean equals(Object other) {
        if(other == null || !(other instanceof LinguisticTree))
            return false;
        LinguisticTree oTree = (LinguisticTree) other;
        if(leaf != null){
            return oTree.getLeaf()!=null
                    && leaf.getSerialization().equals(oTree.getLeaf().getSerialization());
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



    @Override
    public int hashCode() {
        return serialize().hashCode();
    }


    public void resetCachedProperties(){
        serializationPL = null;
        leafCount = -1;
        partitions = null;
        if(parent!=null){
            parent.resetCachedProperties();
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
        left.setParent(null);
        leftChild = new LinguisticTree(left.getLabel());
        resetCachedProperties();
        //resetLeftPositions(leftPos);
        return left;
    }

    public LinguisticTree deleteRightChild(){
        LinguisticTree right = rightChild;
        right.setParent(null);
        rightChild = new LinguisticTree(right.getLabel());
        resetCachedProperties();
        //resetRightPositions(rightPos);
        return right;
    }

    public void setLeftChild(LinguisticTree leftChild) {
        this.leftChild = leftChild;
        resetLeftPositions(leftPos);
        resetCachedProperties();
        leftChild.parent = this;
    }

    public void setRightChild(LinguisticTree rightChild) {
        this.rightChild = rightChild;
        resetRightPositions(rightPos);
        resetCachedProperties();
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

    public String serialize() {

            if(serializationPL == null){
                if(leaf!=null) {
                    serializationPL = IO.escape(leaf.serialize(), escapeAbleChars, charEscape);
                }
                else {
                    if(noChildren())
                        serializationPL = label.toString();
                    else
                        serializationPL = charOpen + (leftChild != null ? leftChild.serialize() : charNull + "") + charSeperate + (rightChild != null ? rightChild.serialize() : charNull + "") + charClose;
                }
            }
            return serializationPL;
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
            Optional<LinguisticType> type = Arrays.stream(LinguisticType.values()).filter(s -> serialization.equals(s.name())).findFirst();
            if(!type.isPresent())
                leaf = new LinguisticToken(serialization);
            else {
                leaf = null;
                label = type.get();
            }
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

                    if(!left.equals(charNull+"")){
                        leftChild = new LinguisticTree(LinguisticType.TREE);
                        leftChild.deserialize(left);
                    }

                    if(!right.equals(charNull+"")){
                        rightChild = new LinguisticTree(LinguisticType.TREE);
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
     * Calculates the height of the tree.
     * If leftChild and rightChild are null (this is a leaf), the height is 0.
     * Otherwise it is the maximum height of the children +1.
     *
     * @return the height of the current tree
     */
    public int getHeight() {
        if(height >= 0)
            return height;
        if (noChildren()) {
            height = 0;
        } else {
            height = Math.max(leftChild != null ? leftChild.getHeight() : 0, rightChild != null ? rightChild.getHeight() : 0) + 1;
        }
        return height;
    }

    public void resetHeight(){
        height = -1;
    }

    public int getMinHeight() {
        if(minHeight >= 0)
            return minHeight;
        if (noChildren()) {
            minHeight = 0;
        } else {
            minHeight = Math.min(leftChild != null ? leftChild.getHeight() : 0, rightChild != null ? rightChild.getHeight() : 0) + 1;
        }
        return minHeight;
    }

    public int getLeftPosition() {
        if(!isFull()) {
            leftPos = -1;
            return leftPos;
        }
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

    public int getRightPosition() {
        if(!isFull()){
            rightPos = -1;
            return rightPos;
        }
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

    public ArrayList<LinguisticTree> getAllSubtrees(int maxHeight) {
        ArrayList<LinguisticTree> result = new ArrayList<>();
        if (getHeight() <= maxHeight || maxHeight < 0)
            result.add(this);
        if (!noChildren()) {
            if (leftChild != null) {
                result.addAll(leftChild.getAllSubtrees(maxHeight));
            }
            if (rightChild != null) {
                result.addAll(rightChild.getAllSubtrees(maxHeight));
            }
        }
        return result;
    }

    public ArrayList<LinguisticTree> getAllCutTrees() {
        ArrayList<LinguisticTree> result = new ArrayList<>();

        if (leftChild != null && rightChild != null)
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(), rightChild.getAllCutTrees(), label));

        if(leaf!=null) {
            result.add(this);
        }
        LinguisticTree emptyTree = new LinguisticTree(LinguisticType.TREE);
        result.add(emptyTree);
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

    public LinkedList<LinguisticTree> getNodes(){
        LinkedList<LinguisticTree> result = new LinkedList<>();
        if(!isEmptyLeaf())
            result.add(this);
        if(leaf!=null)
            return result;

        if(leftChild!=null)
            result.addAll(leftChild.getNodes());
        if(rightChild!=null)
            result.addAll(rightChild.getNodes());
        return result;
    }

    public void calcPartitions(ReconnectedMultiTreeSet treeParts){
        //if(this.serialize().equals("[TREE,[[TREE,TREE],i]]"))
        //    System.out.println();
        long start1, start2;
        start1 = System.currentTimeMillis();
        Disjunction<LinguisticTree> result = new Disjunction<>();
        if(isEmptyLeaf()){
            result.addOperand(copyThis());
            partitions = result;
            t1+=System.currentTimeMillis()-start1;
            c1+=result.size();
            return;
        }
        if(treeParts.containsKey(this) && treeParts.getKey(this).getPartitions()!=null){
            partitions = treeParts.getKey(this).getPartitions();
            return;
        }

        setParents(null);

        LinkedList<LinguisticTree> nodesList = getNodes();
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
            start2 = System.currentTimeMillis();
            Conjunction<LinguisticTree> current = constructPartition(0, nodes, nodeBackups, leftPositions, rightPositions, true, treeParts);
            t2+=System.currentTimeMillis()-start2;
            //result.deepFlatten();
            result.addOperand(current);
        }while(incCut(nodes, nodeBackups, leftPositions, rightPositions, treeParts));

        //result.addOperand(new LinguisticTree(LinguisticType.TREE));
        partitions = result;
        t1+=System.currentTimeMillis()-start1;
        c1+=result.size();
    }


    private Conjunction<LinguisticTree> constructPartition(int pos, LinguisticTree[] nodes, LinguisticTree[] nodeBackups, int[] leftPositions, int[] rightPositions, boolean add, ReconnectedMultiTreeSet treeParts){

        boolean found = false;
        Conjunction<LinguisticTree> result = new Conjunction<>();
        long start3 = System.currentTimeMillis();
        LinguisticTree node = nodes[pos];
        t3+=System.currentTimeMillis()-start3;
        long start4 = System.currentTimeMillis();
        LinguisticTree nodeBackup = nodeBackups[pos];
        t4+=System.currentTimeMillis()-start4;

        if(add) {
            long start5 = System.currentTimeMillis();
            found = treeParts.containsKey(node);
            t5+=System.currentTimeMillis()-start5;
            if(found){
                //long start6 = System.currentTimeMillis();
                Disjunction<LinguisticTree> loadedPartitions = treeParts.getKey(node).getPartitions();
                //t6+=System.currentTimeMillis()-start6;
                if(loadedPartitions!=null) {
                    //long start8 = System.currentTimeMillis();
                    //result.addAllTerminals(loadedPartitions.getTerminals());
                    result.addOperand(loadedPartitions);
                    //t8+=System.currentTimeMillis()-start8;
                    //return result;
                }
            }else {
                long start6 = System.currentTimeMillis();
                //result.addOperand(new LinguisticTree(node.serialize(),node.getLabel()));
                result.addOperand(node.copyThis());
                t6 += System.currentTimeMillis() - start6;
            }
        }
        // add left
        if(leftPositions[pos]>0 && (!found || (node.getLeftChild().isEmptyLeaf() && !nodeBackup.getLeftChild().isEmptyLeaf()))){
            Conjunction<LinguisticTree> conjunction = constructPartition(leftPositions[pos],nodes, nodeBackups,leftPositions, rightPositions, node.getLeftChild().isEmptyLeaf() && !nodeBackup.getLeftChild().isEmptyLeaf(), treeParts);
            long start10 = System.currentTimeMillis();
            result.addAllTerminals(conjunction.getTerminals());
            result.addAllOperations(conjunction.getPropositions());
            t10+=System.currentTimeMillis()-start10;
        }

        // add right
        if(rightPositions[pos]>0 && (!found || (node.getRightChild().isEmptyLeaf() && !nodeBackup.getRightChild().isEmptyLeaf()))) {
            Conjunction<LinguisticTree> conjunction = constructPartition(rightPositions[pos], nodes, nodeBackups, leftPositions, rightPositions,node.getRightChild().isEmptyLeaf() && !nodeBackup.getRightChild().isEmptyLeaf(), treeParts);
            long start10 = System.currentTimeMillis();
            result.addAllTerminals(conjunction.getTerminals());
            result.addAllOperations(conjunction.getPropositions());
            t10+=System.currentTimeMillis()-start10;
        }
        //result.flatten();
        return result;
    }

    private static boolean incCut(LinguisticTree[] nodes, LinguisticTree[] nodeBackups, int[] leftPositions, int[] rightPositions, ReconnectedMultiTreeSet treeParts){
        long start3 = System.currentTimeMillis();
        for(int i=0; i<nodes.length; i++){
            LinguisticTree nodeBackup = nodeBackups[i];
            LinguisticTree node = nodes[i];
            if(nodeBackup.getLeaf()!=null)
                continue;
            if(!nodeBackup.getLeftChild().isEmptyLeaf()){//!=null){
                if(node.getLeftChild().isEmptyLeaf()){//==null){
                    //remove cut
                    node.setLeftChild(nodes[leftPositions[i]]);
                    node.getLeftChild().setParent(node);
                }else{
                    nodes[leftPositions[i]] = node.deleteLeftChild();
                    t3+=System.currentTimeMillis()-start3;
                    return true;
                }
            }
            if(!nodeBackup.getRightChild().isEmptyLeaf()){//!=null){
                if(node.getRightChild().isEmptyLeaf()){//==null){
                    //remove cut
                    node.setRightChild(nodes[rightPositions[i]]);
                    node.getRightChild().setParent(node);
                }else{
                    nodes[rightPositions[i]] = node.deleteRightChild();
                    t3+=System.currentTimeMillis()-start3;
                    return true;
                }
            }
        }
        t3+=System.currentTimeMillis()-start3;
        return false;
    }

    public double getCosineSimilarity(LinguisticTree other){
        // TODO: remove new ReconnectedMultiTreeSet
        MultiSet<LinguisticTree> thisTrees = new MultiSet<>(this.getPartitions(new ReconnectedMultiTreeSet(0)).collectTerminals());
        MultiSet<LinguisticTree> otherTrees = new MultiSet<>(other.getPartitions(new ReconnectedMultiTreeSet(0)).collectTerminals());
        return thisTrees.calcCosineSimilarity(otherTrees);
    }

    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList, LinguisticType label) {
        ArrayList<LinguisticTree> result;
        if(leftList!=null && rightList!=null){
            result = new ArrayList<>(leftList.size() * rightList.size());
            for (LinguisticTree left : leftList) {
                for (LinguisticTree right : rightList) {
                    result.add(new LinguisticTree(left, right, label));
                }
            }
        }else{
            if(leftList==null && rightList==null) {
                return combineTreeLists(Collections.singletonList(null), Collections.singletonList(null), label);
            }else{
                if(leftList!=null){
                    return combineTreeLists(leftList, Collections.singletonList(null), label);
                }else{
                    return combineTreeLists(Collections.singletonList(null),rightList, label);
                }
            }
        }
        return result;
    }




    @Override
    public int compareTo(LinguisticTree o) {
        if(o == null)
            return 1;

        if(o.getHeight()!=this.getHeight()){
            if(this.getHeight() < o.getHeight())
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
        return this.serialize();
    }
}
