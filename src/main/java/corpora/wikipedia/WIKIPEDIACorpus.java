package corpora.wikipedia;

import LinguisticEntities.Token;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import corpora.Corpus;
import helper.IO;
import helper.MergedIterator;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Arne on 07.09.2015.
 */
public class WIKIPEDIACorpus implements Corpus {

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

    public Iterator<Token> token() {


        return new TokenIterator(articles.iterator());
    }


    class TokenIterator extends MergedIterator<String, Character, Token>{

        public TokenIterator(Iterator<String> it){
            super(it);
        }

        @Override
        protected Token getElementContent(Character element) {
            return new Token(element);
        }

        @Override
        protected Iterator<Character> getInnerIterator(String outerElement) {
            ImmutableList<Character> chars = Lists.charactersOf(outerElement);
            UnmodifiableListIterator<Character> iter = chars.listIterator();
            return iter;
        }
    }

}
