package abinder.langanalyzer.helper;

/**
 * Created by Arne on 01.10.2015.
 */
public class Disjunction<V extends Comparable<V>> extends Proposition<V> {
    public Disjunction(){
        super("+");
    }

    @Override
    public double calc(double oa, double ob) {
        return oa+ob;
    }

    @Override
    public void deepFlatten() {
        Proposition<V> result = new Disjunction<>();
        result.addAllTerminals(this.getTerminals());
        for(Proposition proposition : propositions){
            proposition.deepFlatten();
            proposition.flatten();
        }
        flatten();
    }
}
