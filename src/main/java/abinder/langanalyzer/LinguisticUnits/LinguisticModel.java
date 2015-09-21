package abinder.langanalyzer.LinguisticUnits;

/**
 * Created by Arne on 15.09.2015.
 */
public class LinguisticModel {

    public LinguisticLayer_dep<Character> layer = new LinguisticLayer_dep<>("Character");

    public void feed(char character){
        layer.add(new Character(layer, character), character+"");
    }
}
