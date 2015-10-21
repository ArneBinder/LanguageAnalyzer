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

    String serialization;
    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    int position;

    private static final char charEscape = '\\';
    private static final char charPosSeperator = '#';
    private static final char charTypeSeperator = ':';
    private static final char charTokenSeperator = ';';
    private static final char charOpen = '(';
    private static final char charClose = ')';
    private static final HashSet<Character> escapeAbleChars = new HashSet<>(Arrays.asList(charEscape, charOpen, charClose, charPosSeperator,charTypeSeperator,charTokenSeperator, '\n'));


    /*public LinguisticToken(LinguisticType type){
        this.serialization = type;
    }*/

    public LinguisticToken(String serialization){
        deserialize(serialization);
    }

    /*public LinguisticType getSerialization() {
        return serialization;
    }*/

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

    public static String escape(String string){
        return IO.escape(string,escapeAbleChars,charEscape);
    }

    /*public ArrayList<LinguisticTree> getAllTrees(int maxHeight){
        return LinguisticTree.constructTrees(tokens, maxHeight);
    }*/

    /*
    public ArrayList<LinguisticTree> getAllSubtrees(int maxHeight){
        ArrayList<LinguisticTree> result = new ArrayList<>();
        for(LinguisticTree currentTree:getAllTrees(maxHeight)){
            result.addAll(currentTree.getAllSubtrees(maxHeight));
        }
        return result;
    }
    */

    public ArrayList<LinguisticToken> getTokens() {
        return tokens;
    }

    public String serialize(){
        if(serialization!=null)
            return serialization;
        /*String result = (showPosition?position+""+charPosSeperator:"") + IO.escape(type.serialize(),escapeAbleChars,charEscape);
        if(tokens.size()==0)
            return result;
*/
        String result = charOpen+tokens.get(0).serialize();

        for(LinguisticToken token: tokens.subList(1,tokens.size())){
            result +=  charTokenSeperator+token.serialize();
        }
        result += charClose+"";
        return result;
    }

    public void deserialize(String serialization){
        tokens = new ArrayList<>();
        boolean inType = true;
        int open = 0;
        String temp = "";
        int currentPos = -1;
        for(int i=0; i<serialization.length();i++){
            char c = serialization.charAt(i);
            if(c==charEscape){
                i++;
                temp += serialization.charAt(i);
            }else if((inType || open==1) && c==charPosSeperator){
                currentPos = Integer.parseInt(temp);
                temp = "";
            }else if(inType && c==charTypeSeperator){
                this.serialization = temp;
                temp = "";
                inType = false;
            }else if(c==charOpen){
                open++;
            }else if(c==charClose){
                open--;
                if(open==0){
                    LinguisticToken newToken = new LinguisticToken(temp);
                    if(currentPos>=0){
                        newToken.setPosition(currentPos);
                        currentPos = -1;
                    }
                    tokens.add(newToken);
                    temp = "";
                }
            }else if(open==1 && c==charTokenSeperator){
                LinguisticToken newToken = new LinguisticToken(temp);
                if(currentPos>=0){
                    newToken.setPosition(currentPos);
                    currentPos = -1;
                }
                tokens.add(newToken);
                temp = "";
            }else{
                temp += c;
            }
        }
        if(inType){
            this.serialization = temp;
            if(currentPos >=0)
                position = currentPos;
            //temp = "";
        }
    }

    @Override
    public int compareTo(LinguisticToken o) {
        if(o==null)
            return 1;
        return this.serialize().compareTo(o.serialize());
    }

    @Override
    public String toString(){
        return this.serialize();
    }
}
