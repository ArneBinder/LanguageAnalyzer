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

    @Override
    public void deepFlatten() {
        Operation result = new Sum();
        Product product;
        for(Operation operation:operations){
            operation.deepFlatten();
            operation.flatten();
        }
        flatten();

        int[] indices = new int[operations.size()];
        do {
             product = new Product();
             product.addAllTerminals(terminals);
             int i=0;
             for (Operation operation : operations) {
                 if(indices[i]<operation.terminals.size())
                    product.addOperand(operation.terminals.get(indices[i]));
                 else
                     product.addOperand(operation.operations.get(indices[i]-operation.terminals.size()));
                 i++;
             }
            result.addOperand(product);
        }while(incIndices(indices));
        terminals.clear();
        operations.clear();
        operations.add(result);
    }

    private boolean incIndices(int[] indices){
        for(int i=indices.length-1; i>=0; i--){
            indices[i]++;
            if(indices[i]==operations.get(i).size()){
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
