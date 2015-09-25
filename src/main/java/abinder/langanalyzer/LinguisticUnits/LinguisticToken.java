package abinder.langanalyzer.LinguisticUnits;

import abinder.langanalyzer.helper.IO;

import java.lang.*;
import java.lang.Character;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Arne on 17.09.2015.
 */
public class LinguisticToken implements Comparable<LinguisticToken>{

    LinguisticType type;
    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    int position;

    private static final char charEscape = '\\';
    private static final char charPosSeperator = ':';
    private static final char charOpen = '(';
    private static final char charClose = ')';
    private static final HashSet<Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charPosSeperator));


    public LinguisticToken(LinguisticType type){
        this.type = type;
    }

    public LinguisticType getType() {
        return type;
    }

    public void feed(LinguisticToken token){
        token.setPosition(tokens.size());
        tokens.add(token);
        //TODO: all the other stuff!
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /*public ArrayList<LinguisticTree> getAllTrees(int maxDepth){
        return LinguisticTree.constructTrees(tokens, maxDepth);
    }*/

    /*
    public ArrayList<LinguisticTree> getAllSubtrees(int maxDepth){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        for(LinguisticTree currentTree:getAllTrees(maxDepth)){
            result.addAll(currentTree.getAllSubtrees(maxDepth));
        }
        return result;
    }
    */

    public ArrayList<LinguisticToken> getTokens() {
        return tokens;
    }

    public String serialize(boolean showPosition){
        if(tokens.size()==0)
            return (showPosition?position+""+charPosSeperator:"") + IO.escape(type.serialize(),escapeAbleChars,charEscape);
        String result = "";
        for(LinguisticToken token: tokens){
            result += token.serialize(showPosition);
        }
        return(showPosition?position+""+charPosSeperator:"")+charOpen+result+charClose;

    }

    @Override
    public int compareTo(LinguisticToken o) {
        if(o==null)
            return 1;
        return this.type.compareTo(o.getType());
    }
}
