//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.09.05 um 03:10:13 PM CEST 
//


package abinder.langanalyzer.corpora.tiger.generated;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java-Klasse für bodyType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="bodyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="subcorpus" type="{}subcorpusType"/&gt;
 *         &lt;element name="s" type="{}sentenceType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bodyType", propOrder = {
    "subcorpusOrS"
})
public class BodyType {

    @XmlElements({
        @XmlElement(name = "subcorpus", type = SubcorpusType.class),
        @XmlElement(name = "s", type = SentenceType.class)
    })
    protected List<Object> subcorpusOrS;

    /**
     * Gets the value of the subcorpusOrS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subcorpusOrS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubcorpusOrS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubcorpusType }
     * {@link SentenceType }
     * 
     * 
     */
    public List<Object> getSubcorpusOrS() {
        if (subcorpusOrS == null) {
            subcorpusOrS = new ArrayList<Object>();
        }
        return this.subcorpusOrS;
    }

}
