package abinder.langanalyzer.helper;

/**
 * Created by Arne on 01.10.2015.
 */
public class Sum extends Operation {
    public Sum(){
        super("+");
    }

    @Override
    public double calc(double oa, double ob) {
        return oa+ob;
    }
}
