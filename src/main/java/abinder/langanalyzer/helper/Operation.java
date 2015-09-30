package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Created by Arne on 30.09.2015.
 */
public abstract class Operation {
    String operator;
    ArrayList<Operation> operations = new ArrayList<>(1);
    ArrayList<LinguisticTree> terminals = new ArrayList<>(1);

    public Operation(String operator){
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public ArrayList<LinguisticTree> getTerminals() {
        return terminals;
    }

    public void addOperand(Operation operand){
        operations.add(operand);
    }

    public void addOperand(LinguisticTree operand){
        terminals.add(operand);
    }

    public void flatten(){
        ArrayList<Operation> tempOps = new ArrayList<>();
        ArrayList<LinguisticTree> tempTers = new ArrayList<>();
        Iterator<Operation> it = operations.iterator();
        while(it.hasNext()){
            Operation op = it.next();
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

    public String toString(){
        flatten();
        Collections.sort(terminals);
        String joinedTerminals = terminals.stream()
                .map(LinguisticTree::toString)
                .collect(Collectors.joining(" " + operator + " "));
        String joinedOperations = operations.stream()
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

}