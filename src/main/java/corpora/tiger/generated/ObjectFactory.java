//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.05 um 03:10:13 PM CEST 
//


package corpora.tiger.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the corpora.tiger.generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Head_QNAME = new QName("", "head");
    private final static QName _Subcorpus_QNAME = new QName("", "subcorpus");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: corpora.tiger.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HeadType }
     * 
     */
    public HeadType createHeadType() {
        return new HeadType();
    }

    /**
     * Create an instance of {@link SubcorpusType }
     * 
     */
    public SubcorpusType createSubcorpusType() {
        return new SubcorpusType();
    }

    /**
     * Create an instance of {@link Corpus }
     * 
     */
    public Corpus createCorpus() {
        return new Corpus();
    }

    /**
     * Create an instance of {@link BodyType }
     * 
     */
    public BodyType createBodyType() {
        return new BodyType();
    }

    /**
     * Create an instance of {@link MetaType }
     * 
     */
    public MetaType createMetaType() {
        return new MetaType();
    }

    /**
     * Create an instance of {@link AnnotationType }
     * 
     */
    public AnnotationType createAnnotationType() {
        return new AnnotationType();
    }

    /**
     * Create an instance of {@link FeatureType }
     * 
     */
    public FeatureType createFeatureType() {
        return new FeatureType();
    }

    /**
     * Create an instance of {@link EdgelabelType }
     * 
     */
    public EdgelabelType createEdgelabelType() {
        return new EdgelabelType();
    }

    /**
     * Create an instance of {@link FeaturevalueType }
     * 
     */
    public FeaturevalueType createFeaturevalueType() {
        return new FeaturevalueType();
    }

    /**
     * Create an instance of {@link SentenceType }
     * 
     */
    public SentenceType createSentenceType() {
        return new SentenceType();
    }

    /**
     * Create an instance of {@link GraphType }
     * 
     */
    public GraphType createGraphType() {
        return new GraphType();
    }

    /**
     * Create an instance of {@link TerminalsType }
     * 
     */
    public TerminalsType createTerminalsType() {
        return new TerminalsType();
    }

    /**
     * Create an instance of {@link TType }
     * 
     */
    public TType createTType() {
        return new TType();
    }

    /**
     * Create an instance of {@link NonterminalsType }
     * 
     */
    public NonterminalsType createNonterminalsType() {
        return new NonterminalsType();
    }

    /**
     * Create an instance of {@link NtType }
     * 
     */
    public NtType createNtType() {
        return new NtType();
    }

    /**
     * Create an instance of {@link EdgeType }
     * 
     */
    public EdgeType createEdgeType() {
        return new EdgeType();
    }

    /**
     * Create an instance of {@link SecedgeType }
     * 
     */
    public SecedgeType createSecedgeType() {
        return new SecedgeType();
    }

    /**
     * Create an instance of {@link MatchesType }
     * 
     */
    public MatchesType createMatchesType() {
        return new MatchesType();
    }

    /**
     * Create an instance of {@link MatchType }
     * 
     */
    public MatchType createMatchType() {
        return new MatchType();
    }

    /**
     * Create an instance of {@link VarType }
     * 
     */
    public VarType createVarType() {
        return new VarType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HeadType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "head")
    public JAXBElement<HeadType> createHead(HeadType value) {
        return new JAXBElement<HeadType>(_Head_QNAME, HeadType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubcorpusType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "subcorpus")
    public JAXBElement<SubcorpusType> createSubcorpus(SubcorpusType value) {
        return new JAXBElement<SubcorpusType>(_Subcorpus_QNAME, SubcorpusType.class, null, value);
    }

}
