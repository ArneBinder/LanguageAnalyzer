package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;
import abinder.langanalyzer.LinguisticUnits.LinguisticType;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by Arne on 30.09.2015.
 */
public abstract class Proposition<V extends Comparable<V>> implements Comparable<Proposition<V>> {
    String operator;
    LinkedList<Proposition<V>> propositions = new LinkedList<>();
    LinkedList<V> terminals = new LinkedList<>();

    public abstract double calc(double oa, double ob);
    public abstract void deepFlatten();
    //protected double addProb(MultiSet<V> treePattern, V terminal){

    //}

    public Proposition(String operator){
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public LinkedList<Proposition<V>> getPropositions() {
        return propositions;
    }

    public LinkedList<V> getTerminals() {
        return terminals;
    }

    public void addOperand(Proposition<V> operand){
        propositions.add(operand);
    }

    public void addOperand(V operand){
        terminals.add(operand);
    }

    public void addAllOperations(Collection<Proposition<V>> operands){
        propositions.addAll(operands);
    }

    public void addAllTerminals(Collection<V> operands){
        terminals.addAll(operands);
    }

    public void flatten(){
        LinkedList<Proposition<V>> tempOps = new LinkedList<>();
        LinkedList<V> tempTers = new LinkedList<>();
        Iterator<Proposition<V>> it = propositions.iterator();
        while(it.hasNext()){
            Proposition<V> op = it.next();
            if(op.getOperator().equals(operator) || op.getPropositions().size()+op.getTerminals().size() <= 1){
                tempOps.addAll(op.getPropositions());
                tempTers.addAll(op.getTerminals());
                it.remove();
            }
        }
        propositions.addAll(tempOps);
        terminals.addAll(tempTers);

    }

    public int size(){
        return propositions.size()+terminals.size();
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
        if(propositions.size()>0){
            Iterator<Proposition<V>> it = propositions.iterator();
            if(result==0.0)
                result = it.next().calculate(treePattern);
            while(it.hasNext()){
                result = calc(result,it.next().calculate(treePattern));
            }
        }
        // TODO: fix this temporary solution!
        result += treePattern.getRelFrequ((V)new LinguisticTree(LinguisticType.TREE));
        return result;
    }

    public ArrayList<V> collectTerminals(){
        ArrayList<V> result = new ArrayList<>();
        result.addAll(terminals);
        for(Proposition<V> proposition : propositions){
            result.addAll(proposition.collectTerminals());
        }
        return result;
    }

    public String toString(){
        flatten();
        String joinedTerminals = terminals.stream()
                .sorted()
                .map(V::toString)
                .collect(Collectors.joining(" " + operator + " "));
        String joinedOperations = propositions.stream()
                .sorted()
                .map(Proposition::toString)
                .collect(Collectors.joining(" " + operator + " "));

        String result = joinedTerminals;
        if(result!= null && !result.equals("") && joinedOperations!=null && !joinedOperations.equals("")){
            result += " " + operator + " " + joinedOperations;
        }
        if(result==null || result.equals("")){
            result = joinedOperations;
        }
        if(terminals.size()+ propositions.size()>1 && operator.equals("+"))
            return "("+result+")";
        else return result;
    }

    @Override
    public int compareTo(@NotNull Proposition<V> other){
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

            for(int i=0; i< propositions.size();i++){
                result = propositions.get(i).compareTo(other.propositions.get(i));
                if(result!=0)
                    return result;
            }
        }
        return result;
    }

}
