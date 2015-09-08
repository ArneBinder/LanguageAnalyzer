//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.05 um 03:10:13 PM CEST 
//


package corpora.tiger.generated;

import javax.xml.bind.annotation.*;


/**
 * <p>Java-Klasse für graphType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="graphType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="terminals" type="{}terminalsType"/&gt;
 *         &lt;element name="nonterminals" type="{}nonterminalsType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="root" use="required" type="{}idrefType" /&gt;
 *       &lt;attribute name="discontinuous" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "graphType", propOrder = {
    "terminals",
    "nonterminals"
})
public class GraphType {

    @XmlElement(required = true)
    protected TerminalsType terminals;
    @XmlElement(required = true)
    protected NonterminalsType nonterminals;
    @XmlAttribute(name = "root", required = true)
    @XmlIDREF
    protected Object root;
    @XmlAttribute(name = "discontinuous")
    protected Boolean discontinuous;

    /**
     * Ruft den Wert der terminals-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TerminalsType }
     *     
     */
    public TerminalsType getTerminals() {
        return terminals;
    }

    /**
     * Legt den Wert der terminals-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TerminalsType }
     *     
     */
    public void setTerminals(TerminalsType value) {
        this.terminals = value;
    }

    /**
     * Ruft den Wert der nonterminals-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NonterminalsType }
     *     
     */
    public NonterminalsType getNonterminals() {
        return nonterminals;
    }

    /**
     * Legt den Wert der nonterminals-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NonterminalsType }
     *     
     */
    public void setNonterminals(NonterminalsType value) {
        this.nonterminals = value;
    }

    /**
     * Ruft den Wert der root-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Legt den Wert der root-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRoot(Object value) {
        this.root = value;
    }

    /**
     * Ruft den Wert der discontinuous-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDiscontinuous() {
        if (discontinuous == null) {
            return false;
        } else {
            return discontinuous;
        }
    }

    /**
     * Legt den Wert der discontinuous-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDiscontinuous(Boolean value) {
        this.discontinuous = value;
    }

}
