package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.IO;

import java.lang.*;
import java.lang.Character;
import java.util.*;

/**
 * Created by Arne on 17.09.2015.
 */
public class LinguisticTree {
    private LinguisticTree leftChild;
    private LinguisticTree rightChild;
    private LinguisticToken leaf;

    //caching
    private String serialization = null;
    private String serializationPL = null;
    private int depth = -1;
    private int leftPos = -1;
    private int rightPos = -1;

    private static final char charEscape = '\\';
    private static final char charOpen = '[';
    private static final char charClose = ']';
    private static final char charSeperate = ',';
    private static final HashSet<java.lang.Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charSeperate));
    private static final char charNull = 'X';


    public LinguisticTree(LinguisticToken token) {
        leaf = token;
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public boolean isLeaf() {
        return (leftChild == null && rightChild == null);
    }


    @Override
    public boolean equals(Object other) {
        return (other instanceof LinguisticTree)
                && ((LinguisticTree) other).serialize(true).equals(this.serialize(true));
    }

    @Override
    public int hashCode() {
        return serialize(true).hashCode();
    }

    public boolean equalsPositionIndependent(LinguisticTree other) {
        return this.serialize(false).equals(other.serialize(false));
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

    public int getLeftPosition(){
        if(leftPos>=0)
            return leftPos;
        if(isLeaf())
            leftPos = leaf.getPosition();
        else
            leftPos = leftChild.getLeftPosition();
        return leftPos;
    }

    public int getRightPosition(){
        if(rightPos>=0)
            return rightPos;
        if(isLeaf())
            rightPos = leaf.getPosition();
        else
            rightPos = rightChild.getRightPosition();
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

    public ArrayList<LinguisticTree> getAllCutTrees(int maxDepth) {
        ArrayList<LinguisticTree> result = new ArrayList<>();

        if (maxDepth < getDepth())
            return result;
        //if(!isLeaf())
        result.add(this);
        if (leftChild != null) {
            result.addAll(combineTreeLists(leftChild.getAllCutTrees(maxDepth - 1), Collections.singletonList(null)));
        }
        if (rightChild != null) {
            result.addAll(combineTreeLists(Collections.singletonList(null), rightChild.getAllCutTrees(maxDepth - 1)));
        }
        return result;
    }


    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList) {
        ArrayList<LinguisticTree> result = new ArrayList<>(leftList.size() * rightList.size());
        for (LinguisticTree left : leftList) {
            for (LinguisticTree right : rightList) {
                result.add(new LinguisticTree(left, right));
            }
        }
        return result;
    }

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
}
