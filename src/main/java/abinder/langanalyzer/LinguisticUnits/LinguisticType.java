package abinder.langanalyzer.LinguisticUnits;

/**
 * Created by Arne on 17.09.2015.
 */
public enum LinguisticType {
    TREE(0);

    int id;
    // model
    LinguisticType(int id){
        this.id = id;
    }

    /*LinguisticType(char id){
        this.id = id;
    }*/

    /*public LinguisticType(String id){
        this.id = id.charAt(0);
    }*/

    public int getId() {
        return id;
    }

    /*public String serialize(){
        return id+"";
    }*/

    /*@Override
    public int compareTo(LinguisticType o) {
        if(o == null)
            return 1;
        if(id < o.getId())
            return -1;
        if(id > o.getId())
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o){
        return (o!=null)
                && (o instanceof LinguisticType)
                && ((LinguisticType)o).getId() == id;
    }*/
}
