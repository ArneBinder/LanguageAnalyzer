package abinder.langanalyzer.LinguisticUnits;

/**
 * Created by Arne on 17.09.2015.
 */
public class LinguisticType {

    char id;
    // model
    public LinguisticType(char id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String serialize(){
        return ((char)id)+"";
    }
}
