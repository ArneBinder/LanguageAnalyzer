package helper;

import java.util.Iterator;

/**
 * Created by Arne on 05.09.2015.
 */
public abstract class MergedIterator<OuterType, InnerType, ElementType> implements Iterator<ElementType> {

    private Iterator<OuterType> outerIterator;
    private Iterator<InnerType> innerIterator = null;
    private OuterType currentOuterElement = null;
    private InnerType lastInnerElement = null;
    //private ElementType filler = null;
    //private boolean fill = false;



    public MergedIterator(Iterator<OuterType> it){
        outerIterator = it;
    }

    /*public MergedIterator(Iterator<OuterType> it, ElementType filler){
        outerIterator = it;
        this.filler = filler;
    }*/

    public boolean hasNext() {
        if(currentOuterElement ==null || !innerIterator.hasNext()){
            if(!outerIterator.hasNext())
                return false;
            currentOuterElement = outerIterator.next();
            innerIterator = getInnerIterator(currentOuterElement);
            lastInnerElement = null;
            //fill = false;

        }
        return innerIterator.hasNext();
    }

    public ElementType next() {

        //if(lastInnerElement!=null)
          //  System.out.println("last:"+lastInnerElement.toString()+" "+ fill);
        /*if(fill && fill(lastInnerElement)) {
            fill = false;
            return filler;
        }*/
        InnerType currentInnerElement = innerIterator.next();
        lastInnerElement = currentInnerElement;
        //fill = true;
        ElementType n = getElementContent(currentInnerElement);
        return n;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract ElementType getElementContent(InnerType element);//{return element.getOtherAttributes().get(new QName("word"));}

    protected abstract Iterator<InnerType> getInnerIterator(OuterType outerElement);//return outerElement.getGraph().getTerminals().getT().iterator();}

    //protected abstract boolean fill(InnerType element);
}
