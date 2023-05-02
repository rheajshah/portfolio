/*  Student information for assignment:
 *
 *  On <MY> honor, <Rhea Shah>, this programming assignment is <MY> own work
 *  and <I> have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: rjs4665
 *  email address: rheajshah@gmail.com
 *  Grader name: Emma Simon
 *
 *  Student 2
 *  UTEID: -
 *  email address: -
 */

import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<? super E>> {
    private LinkedList<E> queue; //internal storage container
    private int size;

    public PriorityQueue(){
        queue = new LinkedList();
    }

    /*
    Adds element to queue based on its priority. Handles ties in priority in a fair way.
    This means when adding an item to the queue with a priority equal to other items in the queue,
    the new item must go behind the items already present.
     */
    public void add(E element){
        if(queue.size() == 0){ //base case, empty queue
            queue.add(element);
        } else{
            boolean added = false;
            for(int i = 0; !added && i < queue.size(); i++){
                if(element.compareTo(queue.get(i)) < 0){ //lower freq = higher priority
                    queue.add(i, element);
                    added = true; //break statement
                }
            }
            //if it's not added yet, then priority is less than any element already in queue,
            //so add element to end of PQ
            if(!added){
                queue.add(element);
            }
        }
        size++;
    }

    /*
    Get method. Returns front element in queue. Does NOT remove it.
     */
    public E peek(){
        E firstElemInQueue = queue.get(0);
        return firstElemInQueue;
    }

    /*
    Remove method. Removes front element in queue
     */
    public E poll(){
        E firstElemInQueue = queue.get(0);
        queue.remove(firstElemInQueue);
        size--;
        return firstElemInQueue;
    }

    /*
    Returns number of elements in PQ
     */
    public int size(){
        return size;
    }

    public String toString(){
        return queue.toString();
    }
}
