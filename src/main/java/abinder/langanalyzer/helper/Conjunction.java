package abinder.langanalyzer.helper;

/**
 * Created by Arne on 01.10.2015.
 */
public class Conjunction<V extends Comparable<V>> extends Proposition<V> {
    public Conjunction() {
        super("o");
    }

    @Override
    public double calc(double oa, double ob) {
        return oa*ob;
    }

    @Override
    public void deepFlatten() {
        Proposition<V> result = new Disjunction<>();
        Conjunction<V> conjunction;
        for(Proposition proposition : propositions){
            proposition.deepFlatten();
            proposition.flatten();
        }
        flatten();

        int[] indices = new int[propositions.size()];
        do {
             conjunction = new Conjunction<>();
             conjunction.addAllTerminals(terminals);
             int i=0;
             for (Proposition<V> proposition : propositions) {
                 if(indices[i]< proposition.terminals.size())
                    conjunction.addOperand(proposition.terminals.get(indices[i]));
                 else
                     conjunction.addOperand(proposition.propositions.get(indices[i]- proposition.terminals.size()));
                 i++;
             }
            result.addOperand(conjunction);
        }while(incIndices(indices));
        terminals.clear();
        propositions.clear();
        propositions.add(result);
    }

    private boolean incIndices(int[] indices){
        for(int i=indices.length-1; i>=0; i--){
            indices[i]++;
            if(indices[i]== propositions.get(i).size()){
                indices[i] = 0;
                if(i==0)
                    return false;
            }else{
                return true;
            }
        }
        return false;
    }
}
