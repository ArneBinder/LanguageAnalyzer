//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.05 um 03:10:13 PM CEST 
//


package corpora.tiger.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java-Klasse für annotationType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="annotationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="feature" type="{}featureType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="edgelabel" type="{}edgelabelType" minOccurs="0"/&gt;
 *         &lt;element name="secedgelabel" type="{}edgelabelType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "annotationType", propOrder = {
    "feature",
    "edgelabel",
    "secedgelabel"
})
public class AnnotationType {

    @XmlElement(required = true)
    protected List<FeatureType> feature;
    protected EdgelabelType edgelabel;
    protected EdgelabelType secedgelabel;

    /**
     * Gets the value of the feature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the feature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureType }
     * 
     * 
     */
    public List<FeatureType> getFeature() {
        if (feature == null) {
            feature = new ArrayList<FeatureType>();
        }
        return this.feature;
    }

    /**
     * Ruft den Wert der edgelabel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EdgelabelType }
     *     
     */
    public EdgelabelType getEdgelabel() {
        return edgelabel;
    }

    /**
     * Legt den Wert der edgelabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EdgelabelType }
     *     
     */
    public void setEdgelabel(EdgelabelType value) {
        this.edgelabel = value;
    }

    /**
     * Ruft den Wert der secedgelabel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EdgelabelType }
     *     
     */
    public EdgelabelType getSecedgelabel() {
        return secedgelabel;
    }

    /**
     * Legt den Wert der secedgelabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EdgelabelType }
     *     
     */
    public void setSecedgelabel(EdgelabelType value) {
        this.secedgelabel = value;
    }

}
