package abinder.langanalyzer.helper;

import abinder.langanalyzer.LinguisticUnits.LinguisticTree;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Arne on 21.10.2015.
 */
public class Utils {

    public static long getCatalan(int n){
        int i = n-1;
        return CombinatoricsUtils.binomialCoefficient(2 * i, i) / (i + 1);
    }

    public static long getSparseTreeCount(int n){
        return getCatalan(n) * (1<<n);
    }

    public static long getTreeCount(int n){
        long result = 0;
        for(int i=1; i<=n; i++)
            result += getSparseTreeCount(i)*(n-i + 1);
        return result;
    }

    public static ArrayList<LinguisticTree> sortedTrees(ArrayList<LinguisticTree> trees){
        ArrayList<LinguisticTree> result = new ArrayList<>(trees);
        Collections.sort(result);
        return result;
    }
}
