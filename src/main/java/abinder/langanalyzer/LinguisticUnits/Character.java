package abinder.langanalyzer.LinguisticUnits;

/**
 * Created by Arne on 05.09.2015.
 */
public class Character extends LinguisticUnit {
    public Character(LinguisticLayer<Character> layer, String character){
        super(layer, character.hashCode());
        this.layer = layer;
    }

    public Character(LinguisticLayer<Character> layer, char character){
        super(layer, character);
        this.layer = layer;
    }


    Boolean isAtomic(){
        return true;
    }

}
