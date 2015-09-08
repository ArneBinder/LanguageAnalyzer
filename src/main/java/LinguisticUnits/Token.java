package LinguisticUnits;

/**
 * Created by Arne on 05.09.2015.
 */
public class Token extends LinguisticUnit {
    char character;
    public Token(LinguisticLayer<Token> layer, String character){
        super(layer);
        this.layer = layer;
        this.character = character==null?0:character.charAt(0);
    }

    public Token(LinguisticLayer<Token> layer, char character){
        super(layer);
        this.layer = layer;
        this.character = character;
    }


    Boolean isAtomic(){
        return true;
    }

    public String toString(){
        return character+"";
    }
}
