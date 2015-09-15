//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.05 um 03:10:13 PM CEST 
//


package abinder.langanalyzer.corpora.tiger.generated;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für sentenceType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="sentenceType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="graph" type="{}graphType" minOccurs="0"/&gt;
 *         &lt;element name="matches" type="{}matchesType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{}idType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sentenceType", propOrder = {
    "graph",
    "matches"
})
public class SentenceType {

    protected GraphType graph;
    protected MatchesType matches;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Ruft den Wert der graph-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GraphType }
     *     
     */
    public GraphType getGraph() {
        return graph;
    }

    /**
     * Legt den Wert der graph-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphType }
     *     
     */
    public void setGraph(GraphType value) {
        this.graph = value;
    }

    /**
     * Ruft den Wert der matches-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MatchesType }
     *     
     */
    public MatchesType getMatches() {
        return matches;
    }

    /**
     * Legt den Wert der matches-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MatchesType }
     *     
     */
    public void setMatches(MatchesType value) {
        this.matches = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
