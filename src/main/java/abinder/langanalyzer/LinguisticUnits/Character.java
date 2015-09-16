package abinder.langanalyzer.LinguisticUnits;

/**
 * Created by Arne on 05.09.2015.
 */
public class Character extends LinguisticUnit {
    //char character;
    public Character(LinguisticLayer<Character> layer, String character){
        super(layer, character.hashCode());
        this.layer = layer;
        //setType(character==null?0:character.charAt(0));
    }

    public Character(LinguisticLayer<Character> layer, char character){
        super(layer, character);
        this.layer = layer;
        //this.character = character;
    }


    Boolean isAtomic(){
        return true;
    }

}
