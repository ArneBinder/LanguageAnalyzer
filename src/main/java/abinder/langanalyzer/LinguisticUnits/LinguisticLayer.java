package abinder.langanalyzer.LinguisticUnits;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arne on 21.09.2015.
 */
public class LinguisticLayer {

    ArrayList<LinguisticToken> tokens = new ArrayList<>();
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> previousTrees = new ArrayList<>();
    ArrayList<ArrayList<ArrayList<LinguisticTree>>> rightTrees = new ArrayList<>();

    int count = 0;
    int posOffset = 0;

    public LinguisticLayer(){
        //previousTrees.add(new ArrayList<>());

    }

    public void feed(LinguisticToken token, int maxDepth){
        token.setPosition(count);


        LinguisticTree firstTokenTree = new LinguisticTree(token);
        ArrayList<LinguisticTree> currentTrees = new ArrayList<>(1);
        currentTrees.add(firstTokenTree);
        ArrayList<ArrayList<LinguisticTree>> newTrees = new ArrayList<>();
        newTrees.add(currentTrees);
        //newTrees.add(currentTrees);
        //if(count - posOffset - 1 >= 0) {
        int currentTokenTreesDepth = 0;
        int currentDepth = 0;
        ArrayList<LinguisticTree> currentTokenTrees = new ArrayList<>(1);

        //currentTokenTrees.add(new LinguisticTree(token));

        while(currentTokenTreesDepth < maxDepth && currentTokenTreesDepth < newTrees.size()) {
            currentTokenTrees = newTrees.get(currentTokenTreesDepth);

            ArrayList<ArrayList<LinguisticTree>> currentNewTrees = new ArrayList<>();
            for(LinguisticTree currentTokenTree: currentTokenTrees) {

                // iterate over maxDepth of previous trees ending at the previous position (count - posOffset)
                if(currentTokenTree.getLeftPosition() - 1 >= 0) {
                    currentDepth = currentTokenTreesDepth;
                    for (ArrayList<LinguisticTree> currentPreviousTrees : previousTrees.get(currentTokenTree.getLeftPosition() - 1)) {
                        currentTrees = new ArrayList<>();
                        if (currentDepth == maxDepth)
                            break;
                        for (LinguisticTree currentPreviousTree : currentPreviousTrees) {
                            LinguisticTree newTree = new LinguisticTree(currentPreviousTree, currentTokenTree);
                            System.out.println(newTree.serialize(false));
                            currentTrees.add(newTree);
                        }

                        for(int i=0; i<currentTrees.size(); i++){
                            if(currentNewTrees.size()<=i)
                                currentNewTrees.add(new ArrayList<>());
                            currentNewTrees.get(i).add(currentTrees.get(i));
                        }
                        //currentNewTrees.add(currentTrees);
                        currentDepth++;
                    }
                }
                System.out.println();
            }
            int i = 0;
            for(ArrayList<LinguisticTree> currentNewTree:currentNewTrees){
                if(i+currentTokenTreesDepth >= newTrees.size())
                    newTrees.add(new ArrayList<>());
                newTrees.get(i+currentTokenTreesDepth).addAll(currentNewTree);
                i++;
            }

            currentTokenTreesDepth++;

        }


        previousTrees.add(newTrees);

        count++;
    }


    public static ArrayList<LinguisticTree> constructTrees2(List<LinguisticToken> tokens, int maxDepth){

        int offset = tokens.get(0).getPosition();

        ArrayList<LinguisticTree> result = new ArrayList<>();
        List<List<LinguisticTree>> lefts = new ArrayList<>(tokens.size());
        List<List<LinguisticTree>> rights = new ArrayList<>(tokens.size());

        ArrayList<LinguisticTree> highestTrees = new ArrayList<>(tokens.size());
        for(LinguisticToken token: tokens){
            LinguisticTree current = new LinguisticTree(token);
            highestTrees.add(current);
            ArrayList<LinguisticTree> l = new ArrayList<>();
            ArrayList<LinguisticTree> r = new ArrayList<>();
            l.add(current);
            r.add(current);
            lefts.add(l);
            rights.add(r);
        }
        result.addAll(highestTrees);


        int leftPos;
        int rightPos;
        //ArrayList<LinguisticTree> newTrees = new ArrayList<>(highestTrees.size());
        for(int currentDepth = 0; currentDepth < maxDepth; currentDepth++) {
            ArrayList<LinguisticTree> newTrees = new ArrayList<>(highestTrees.size());
            for(LinguisticTree highestTree: highestTrees){
                leftPos = highestTree.getLeftPosition();
                rightPos = highestTree.getRightPosition();
                if(leftPos > 0){
                    for(LinguisticTree smallerLeftTree: lefts.get(leftPos - 1 - offset)){
                        LinguisticTree newTree = new LinguisticTree(smallerLeftTree, highestTree);
                        newTrees.add(newTree);
                    }
                }

                if(rightPos < tokens.size()-1){
                    for(LinguisticTree smallerRightTree: rights.get(rightPos + 1 - offset)){
                        if(smallerRightTree.getDepth() < currentDepth) {
                            LinguisticTree newTree = new LinguisticTree(highestTree, smallerRightTree);
                            newTrees.add(newTree);
                        }
                    }
                }

            }
            for(LinguisticTree newTree: newTrees){
                leftPos = newTree.getLeftPosition();
                rightPos = newTree.getRightPosition();
                lefts.get(rightPos).add(newTree);
                rights.get(leftPos).add(newTree);
            }
            result.addAll(newTrees);
            highestTrees = newTrees;
        }



        return result;

    }

}
