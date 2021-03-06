package abinder.langanalyzer.corpora.tiger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableListIterator;
import com.google.gson.Gson;
import abinder.langanalyzer.corpora.Corpus;
import abinder.langanalyzer.corpora.tiger.generated.SentenceType;
import abinder.langanalyzer.corpora.tiger.generated.TType;
import abinder.langanalyzer.helper.MergedIterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Arne on 05.09.2015.
 */
public class TIGERCorpus extends Corpus {

    abinder.langanalyzer.corpora.tiger.generated.Corpus tigerCorpus;

    ArrayList<SentenceType> tigerSentences = new ArrayList<SentenceType>();

    public TIGERCorpus(){}

    TIGERCorpus(abinder.langanalyzer.corpora.tiger.generated.Corpus tigerCorpus){
        Gson gson = new Gson();
        this.tigerCorpus = gson.fromJson(gson.toJson(tigerCorpus), abinder.langanalyzer.corpora.tiger.generated.Corpus.class);
    }

    public void readFromFile(String filename) throws IOException{
        File file = new File(filename);

        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(abinder.langanalyzer.corpora.tiger.generated.Corpus.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            tigerCorpus = (abinder.langanalyzer.corpora.tiger.generated.Corpus)jaxbUnmarshaller.unmarshal( file );
            for(Object o: tigerCorpus.getBody().getSubcorpusOrS()){
                if(o instanceof SentenceType) {
                    tigerSentences.add((SentenceType) o);
                }
            }
            tigerCorpus.getBody().getSubcorpusOrS().clear();
        } catch (JAXBException e) {
            throw new IOException(e.toString());
        }

    }

    public void writeToFile(String filename){
        File file = new File(filename);

        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(abinder.langanalyzer.corpora.tiger.generated.Corpus.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            tigerCorpus.getBody().getSubcorpusOrS().addAll(tigerSentences);
            jaxbMarshaller.marshal(tigerCorpus, file);
            tigerCorpus.getBody().getSubcorpusOrS().clear();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    public Corpus seperateEvalCorpus(double evalPercentage) throws Exception {
        if(evalPercentage >= 1.0){
            throw new Exception("ERROR: evalPercentage >= 1.0. Nothing left in the original corpus");
        }
        int evalSize = (int) (getSentenceCount() * evalPercentage);
        ArrayList<SentenceType> evalSentences = deleteSentences(getSentenceCount() - evalSize, evalSize);

        TIGERCorpus evalCorpus = new TIGERCorpus(tigerCorpus);
        evalCorpus.addSentences(evalSentences);
        return evalCorpus;
    }

    public ArrayList<SentenceType> deleteSentences(int startIndex, int count){
        int i = 0;
        ArrayList<SentenceType> result = new ArrayList<SentenceType>(count);
        Iterator<SentenceType> it = tigerSentences.iterator();
        while(it.hasNext()){
            SentenceType elem = it.next();
            if(i >= startIndex + count){
                break;
            }
            if(i>=startIndex && i < startIndex + count){
                result.add(elem);
                //System.out.println("delete: "+(elem).getId());
                it.remove();
            }
            i++;

        }
        return result;
    }

    public void addSentences(List<SentenceType> sentences){
        this.tigerSentences.addAll(sentences);
    }

    public int getSentenceCount(){
        return tigerSentences.size();
    }

    public Map<String, Object> getMetadata() {
        return null;
    }

    public Iterator<java.lang.Character> tokens() {
        //if(getBasicLayer().getLength()==0){
            return new TokenIterator(new CharacterIterator(tigerSentences.iterator()));
        //}else {
        //    return getBasicLayer().getTokens().iterator();
        //}

    }

    /*public Iterator<String> getCharacterIterator() {
        return new CharacterIterator(tigerSentences.iterator());
    }*/

    public class CharacterIterator extends MergedIterator<SentenceType, TType, String>{

        String last = null;
        public CharacterIterator(Iterator<SentenceType> it){
            super(it);
        }

        protected String getElementContent(TType element){
            String result = "";

            //System.out.println(element.toString());
            if(!element.getOtherAttributes().get(new QName("pos")).startsWith("$") && (last == null || !last.equals("``"))){

                result += " ";
            }

            if(element.getOtherAttributes().get(new QName("word")).equals("``")){

                result += " ";
            }

            last = element.getOtherAttributes().get(new QName("word"));
            result +=  last;
            return result;
        }

        protected Iterator<TType> getInnerIterator(SentenceType outerElement){
            return outerElement.getGraph().getTerminals().getT().iterator();
        }

    }

    public class TokenIterator extends MergedIterator<String, java.lang.Character, java.lang.Character>{
        public TokenIterator(Iterator<String> it){
            super(it);
        }

        protected java.lang.Character getElementContent(java.lang.Character element){
            return element;
        }

        protected Iterator<java.lang.Character> getInnerIterator(String outerElement){
            ImmutableList<java.lang.Character> chars = Lists.charactersOf(outerElement);
            UnmodifiableListIterator<java.lang.Character> iter = chars.listIterator();
            return iter;
        }

    }




}
