package abinder.langanalyzer.helper;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Arne on 22.09.2015.
 */
public class MultiSet<V> extends HashMap<V, Double> implements Iterable<V> {

    protected double totalCount = 0;
    private double maxSize = 1000;

    public MultiSet(int initialCapacity){
        super(initialCapacity);
    }

    public MultiSet(Collection<V> collection){
        super(collection.size());
        for(V value: collection)
            add(value);
    }

    public double getTotalCount() {
        return totalCount;
    }

    public void add(V value){
        double count = 0;
        if(this.containsKey(value))
           count = get(value);
        count++;
        totalCount++;
        put(value, count);
    }

    public void add(V value, double confidence){
        double count = 0;
        if(this.containsKey(value))
            count = get(value);
        count+=confidence;
        totalCount+=confidence;
        put(value, count);
    }


    public double getRelFrequ(V value){
        if(!this.containsKey(value))
            return 0;
        return get(value) / totalCount;
    }

    @Override
    public Iterator<V> iterator() {
        return keySet().iterator();
    }

    public Map<V, Double> sortByValue()
    {
        Map<V, Double> result = new LinkedHashMap<>();
        Stream<Entry<V, Double>> st = this.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    public SortedSet<V> sortedKeySet(){
       return new TreeSet<V>(this.keySet());
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream <Entry<K,V>> st = map.entrySet().stream();

        st.sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }

    public double calcCosineSimilarity(MultiSet<V> other) {
        if(this.size()==0 || other.size() == 0)
            return Double.POSITIVE_INFINITY;
        double sqsuma = 0;
        double sqsumb = 0;
        double divident = 0;
        for(V elema: this.keySet()){
            double counta = get(elema);
            sqsuma += counta*counta;
            Double countb = other.get(elema);
            if(countb!=null){
                divident += counta*countb;
                sqsumb += countb*countb;
            }
        }

        sqsumb += other.entrySet().stream()
                .filter(element -> !containsKey(element.getKey()))
                .mapToDouble(Entry::getValue)
                .map(e -> e*e)
                .sum();

        double divisor = sqsuma*sqsumb;
        if(divisor==0.0)
            return Double.POSITIVE_INFINITY;
        return divident/Math.sqrt(divisor);
    }

    public void clean(){
        double overhead = totalCount - maxSize;
        while(overhead > 0){
            double rem = overhead / size();
            for(Iterator<Map.Entry<V, Double>> it = entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<V, Double> entry = it.next();
                double newValue = entry.getValue() - rem;
                if(newValue > 0) {
                    entry.setValue(newValue);
                    overhead -= rem;
                }else{
                    overhead -= entry.getValue();
                    it.remove();
                }
            }
        }

    }
}
