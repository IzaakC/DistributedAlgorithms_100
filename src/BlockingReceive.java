import java.util.LinkedList;
import java.util.Queue;

public class BlockingReceive {
    private Queue<Integer> queue = new LinkedList<Integer>();

    public synchronized void set(int element) throws InterruptedException {
        queue.add(element);
        notify(); 
    }

    public synchronized int get() throws InterruptedException {
        while(queue.isEmpty()) {
            // System.out.println("waiting..");
            wait();
        }
        // System.out.println("Done waiting..");
        notify();
        return queue.remove();
    }
}
