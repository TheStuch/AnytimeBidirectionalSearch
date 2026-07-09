import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class MinHeapImpl<E> {
    protected E[] elements;
    protected int count = 0;
    private Map<E, Integer> locations = new HashMap<>();
    private final Comparator<E> comparator;

    public MinHeapImpl(Comparator<E> comparator){
        this.elements = (E[]) new Object[4];
        this.comparator = comparator;
    }

    public void reHeapify(E element){
        int index = this.getArrayIndex(element);
        if (index == -1){
            throw new NoSuchElementException("MinHeap does not contain " + element);
        }
        if (index > 1 && isGreater(index / 2, index)){
            upHeap(index);
        } else if (index * 2 <= count && (isGreater(index, index * 2) || (elements[index * 2 + 1] != null && isGreater(index, index * 2 + 1)))){
            downHeap(index);
        }
    }

    public void reHeapify(int index){
        if (index == -1){
            throw new NoSuchElementException("Element was already popped");
        }
        if (index > 1 && isGreater(index / 2, index)){
            upHeap(index);
        } else if (index * 2 <= count && (isGreater(index, index * 2) || (elements[index * 2 + 1] != null && isGreater(index, index * 2 + 1)))){
            downHeap(index);
        }
    }

    //returns location of element
    //if not present, returns MAX
    public int getArrayIndex(E element){
        if(locations.containsKey(element)){
            return locations.get(element);
        }
        return Integer.MAX_VALUE;
    }

    private void doubleArraySize(){
        E[] copy = (E[]) new Object[this.elements.length * 2];
        for (int i = 1; i <= count; i++){
            copy[i] = elements[i];
        }
        this.elements = copy;
    }

     public boolean isEmpty() {
        return this.count == 0;
     }

    /**
     * is elements[i] > elements[j]?
     */

    private boolean isGreater(int i, int j) {
        return comparator.compare(this.elements[i], this.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */

    private void swap(int i, int j) {
        E temp = this.elements[i];
        int tempLoc = locations.get(temp);
        locations.put(this.elements[i], j);
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        locations.put(this.elements[i], tempLoc);
    }

    /**
     * while the key at index k is less than its
     * parent's key, swap its contents with its parent’s
     */
    private void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k)) {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    private void downHeap(int k) {
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }

    public void insert(E x) {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1) {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        locations.put(x, this.count);
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    public E getCopy(int index){
        if(index < 1 || index > count){
            return null;
        }
        return elements[index];
    }

   public E peek(){
        return this.elements[1];
    }

   public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        this.locations.put(min, -1); // -1 means it was removed
        return min;
    }
}