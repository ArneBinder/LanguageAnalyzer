package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Arne on 30.09.2015.
 */
public abstract class Operation<V extends Comparable<V>> implements Comparable<Operation<V>> {
    String operator;
    LinkedList<Operation<V>> operations = new LinkedList<>();
    LinkedList<V> terminals = new LinkedList<>();

    public abstract double calc(double oa, double ob);
    public abstract void deepFlatten();

    public Operation(String operator){
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public LinkedList<Operation<V>> getOperations() {
        return operations;
    }

    public LinkedList<V> getTerminals() {
        return terminals;
    }

    public void addOperand(Operation<V> operand){
        operations.add(operand);
    }

    public void addOperand(V operand){
        terminals.add(operand);
    }

    public void addAllOperations(Collection<Operation<V>> operands){
        operations.addAll(operands);
    }

    public void addAllTerminals(Collection<V> operands){
        terminals.addAll(operands);
    }

    public void flatten(){
        LinkedList<Operation<V>> tempOps = new LinkedList<>();
        LinkedList<V> tempTers = new LinkedList<>();
        Iterator<Operation<V>> it = operations.iterator();
        while(it.hasNext()){
            Operation<V> op = it.next();
            if(op.getOperator().equals(operator) || op.getOperations().size()+op.getTerminals().size() <= 1){
                tempOps.addAll(op.getOperations());
                tempTers.addAll(op.getTerminals());
                it.remove();
            }
        }
        operations.addAll(tempOps);
        terminals.addAll(tempTers);

    }

    public int size(){
        return operations.size()+terminals.size();
    }

    public double calculate(MultiSet<V> treePattern){
        double result=0.0;
        if(size()==0)
            return result;
        if(terminals.size()>0){
            Iterator<V> it = terminals.iterator();
            result = treePattern.getRelFrequ(it.next());
            while(it.hasNext()){
                result = calc(result,treePattern.getRelFrequ(it.next()));
            }
        }
        if(operations.size()>0){
            Iterator<Operation<V>> it = operations.iterator();
            if(result==0.0)
                result = it.next().calculate(treePattern);
            while(it.hasNext()){
                result = calc(result,it.next().calculate(treePattern));
            }
        }
        return result;
    }

    public ArrayList<V> collectTerminals(){
        ArrayList<V> result = new ArrayList<>();
        result.addAll(terminals);
        for(Operation<V> operation: operations){
            result.addAll(operation.collectTerminals());
        }
        return result;
    }

    public String toString(){
        flatten();
        String joinedTerminals = terminals.stream()
                .sorted()
                .map(V::toString)
                .collect(Collectors.joining(" " + operator + " "));
        String joinedOperations = operations.stream()
                .sorted()
                .map(Operation::toString)
                .collect(Collectors.joining(" " + operator + " "));

        String result = joinedTerminals;
        if(result!= null && !result.equals("") && joinedOperations!=null && !joinedOperations.equals("")){
            result += " " + operator + " " + joinedOperations;
        }
        if(result==null || result.equals("")){
            result = joinedOperations;
        }
        if(terminals.size()+ operations.size()>1 && operator.equals("+"))
            return "("+result+")";
        else return result;
    }

    @Override
    public int compareTo(@NotNull Operation<V> other){
        if (other==null){
            return 1;
        }
        int result = size()-other.size();
        if(result==0)
            result = other.terminals.size()-terminals.size();
        if(result==0){
            for(int i=0; i<terminals.size();i++){
                result = terminals.get(i).compareTo(other.terminals.get(i));
                if(result!=0)
                    return result;
            }

            for(int i=0; i<operations.size();i++){
                result = operations.get(i).compareTo(other.operations.get(i));
                if(result!=0)
                    return result;
            }
        }
        return result;
    }

}
