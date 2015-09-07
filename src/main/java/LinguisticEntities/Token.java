package LinguisticEntities;

/**
 * Created by Arne on 05.09.2015.
 */
public class Token {
    char character;
    public Token(String character){
        this.character = character==null?0:character.charAt(0);
    }

    public Token(char character){
        this.character = character;
    }


    public String toString(){
        return character+"";
    }
}
