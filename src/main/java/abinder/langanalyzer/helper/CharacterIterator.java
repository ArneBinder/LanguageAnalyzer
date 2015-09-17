package abinder.langanalyzer.helper;

import java.util.Iterator;

/**
 * Created by Arne on 17.09.2015.
 */
public class CharacterIterator implements Iterator<Character> {

    private final String str;
    private int pos = 0;

    public CharacterIterator(String str) {
        this.str = str;
    }

    public boolean hasNext() {
        return pos < str.length();
    }

    public Character next() {
        return str.charAt(pos++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
