package abinder.langanalyzer.helper;

/**
 * Created by Arne on 01.10.2015.
 */
public class Product extends Operation {
    public Product() {
        super("o");
    }

    @Override
    public double calc(double oa, double ob) {
        return oa*ob;
    }
}
