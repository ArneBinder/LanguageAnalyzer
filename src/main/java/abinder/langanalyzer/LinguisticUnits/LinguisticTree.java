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

    private static final char charEscape = '\\';
    private static final char charOpen = '[';
    private static final char charClose = ']';
    private static final char charSeperate = ',';
    private static final HashSet<java.lang.Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charSeperate));
    private static final char charNull = 'X';


    public LinguisticTree(LinguisticToken token){
        leaf=token;
    }

    public LinguisticTree(LinguisticTree leftChild, LinguisticTree rightChild){
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public boolean isLeaf(){
        return (leftChild==null && rightChild==null);
    }


    @Override
    public boolean equals(Object other){
        return (other instanceof LinguisticTree)
                && ((LinguisticTree) other).serialize(true).equals(this.serialize(true));
    }

    @Override
    public int hashCode(){
        return serialize(true).hashCode();
    }

    public boolean equalsPositionIndependent(LinguisticTree other){
        return this.serialize(false).equals(other.serialize(false));
    }


    public String serialize(boolean showPosition){
        if(isLeaf())
            return IO.escape(leaf.serialize(showPosition), escapeAbleChars, charEscape);
        else{
            return  charOpen+(leftChild!=null?leftChild.serialize(showPosition):charNull+"")+charSeperate+(rightChild!=null?rightChild.serialize(showPosition):charNull+"")+charClose;
        }
    }

    /**
     * Calculates the depth of the tree.
     * If leftChild and rightChild are null (this is a leaf), the depth is 0.
     * Otherwise it is the maximum depth of the children +1.
     * @return the depth of the current tree
     */
    public int getDepth(){
        if(isLeaf()){
            return 0;
        }else{
            return Math.max(leftChild!=null?leftChild.getDepth():0,rightChild!=null?rightChild.getDepth():0)+1;
        }
    }

    public ArrayList<LinguisticTree> getAllSubtrees(int maxDepth){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        if(getDepth() <= maxDepth)
            result.add(this);
        if(!isLeaf()){
            if(leftChild!=null){
                result.addAll(leftChild.getAllSubtrees(maxDepth));
            }
            if(rightChild!=null){
                result.addAll(rightChild.getAllSubtrees(maxDepth));
            }
        }
        return result;
    }

    public ArrayList<LinguisticTree> getAllCutTrees(int maxDepth){
        ArrayList<LinguisticTree> result = new ArrayList<>();

        if(maxDepth  < getDepth())
            return result;
        //if(!isLeaf())
        result.add(this);
        if(leftChild!=null) {
             result.addAll(combineTreeLists(leftChild.getAllCutTrees(maxDepth -1), Collections.singletonList(null)));
        }
        if(rightChild!=null){
            result.addAll(combineTreeLists(Collections.singletonList(null),rightChild.getAllCutTrees(maxDepth -1)));
        }
        return result;
    }


    private static ArrayList<LinguisticTree> combineTreeLists(List<LinguisticTree> leftList, List<LinguisticTree> rightList){
        ArrayList<LinguisticTree> result = new ArrayList<>(leftList.size()*rightList.size());
        for(LinguisticTree left:leftList){
            for(LinguisticTree right: rightList){
                result.add(new LinguisticTree(left, right));
            }
        }
        return result;
    }

    public static ArrayList<LinguisticTree> constructTrees(List<LinguisticToken> tokens, int maxDepth){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        if(tokens.size()==1){
            result.add(new LinguisticTree(tokens.get(0)));
            return result;
        }

        for(int i=1; i<tokens.size();i++){
            if(maxDepth <= 0){
                result.addAll(constructTrees(tokens.subList(0, i), maxDepth));
                result.addAll(constructTrees(tokens.subList(i, tokens.size()), maxDepth));
            }else {
                result.addAll(combineTreeLists(constructTrees(tokens.subList(0, i), maxDepth - 1), constructTrees(tokens.subList(i, tokens.size()), maxDepth - 1)));
            }
        }

        return result;
    }
}
