package corpora.wikipedia;

import LinguisticUnits.Character;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import corpora.Corpus;
import helper.IO;
import helper.MergedIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Arne on 07.09.2015.
 */
public class WIKIPEDIACorpus extends Corpus {

    ArrayList<String> articles = new ArrayList<String>();

    /*public void readFromFile(InputStream inputStream) throws IOException {
        articles.add(IO.readFile(inputStream));

    }*/

    public void readFromFile(String filename) throws IOException {
        articles.add(IO.readFile(filename));
    }

    public void writeToFile(String filename) {

    }

    public Corpus seperateEvalCorpus(double evalPercentage) throws Exception {
        return null;
    }

    public Map<String, Object> getMetadata() {
        return null;
    }

    public Iterator<java.lang.Character> tokens() {
        //if(getBasicLayer().getLength()==0)
            return new TokenIterator(articles.iterator());
        //else
        //    return getBasicLayer().getTokens().iterator();
    }


    class TokenIterator extends MergedIterator<String, java.lang.Character, java.lang.Character>{

        public TokenIterator(Iterator<String> it){
            super(it);
        }

        @Override
        protected java.lang.Character getElementContent(java.lang.Character element) {
            return element;
        }

        @Override
        protected Iterator<java.lang.Character> getInnerIterator(String outerElement) {
            ImmutableList<java.lang.Character> chars = Lists.charactersOf(outerElement);
            UnmodifiableListIterator<java.lang.Character> iter = chars.listIterator();
            return iter;
        }
    }

}
