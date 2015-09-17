package abinder.langanalyzer.LinguisticUnits;

import java.lang.*;
import java.util.ArrayList;

/**
 * Created by Arne on 17.09.2015.
 */
public class LinguisticToken {
    LinguisticType type;
    ArrayList<LinguisticToken> tokens = new ArrayList<>();

    public LinguisticToken(LinguisticType type){
        this.type = type;
    }

    public void feed(LinguisticToken token){
        tokens.add(token);
        //TODO: all the other stuff!
    }

    public ArrayList<LinguisticTree> getAllTrees(int maxDepth){
        return LinguisticTree.constructTrees(tokens, maxDepth);
    }

    public ArrayList<LinguisticTree> getAllSubtrees(int maxDepth){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        for(LinguisticTree currentTree:getAllTrees(maxDepth)){
            result.addAll(currentTree.getAllSubtrees(maxDepth));
        }
        return result;
    }

    public String serialize(){
        if(tokens.size()==0)
            return type.serialize();
        String result = "";
        for(LinguisticToken token: tokens){
            result += token.serialize();
        }
        return result;

    }
}
