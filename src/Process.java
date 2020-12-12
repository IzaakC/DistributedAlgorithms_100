import java.rmi.*;
// import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.*;

public class Process extends UnicastRemoteObject implements ProcessInterface, Runnable {
    public int pid, port, n, f;
    BlockingQueue<Msg> proposal_queue = new LinkedBlockingDeque<Msg>();
    BlockingQueue<Msg> notification_queue = new LinkedBlockingDeque<Msg>();
    BlockingQueue<Integer> start_block = new LinkedBlockingDeque<Integer>();
    
    // constructor, save pid, port and calculate the neighbor id
    public Process(int _pid, int _n, int _f, int _port) throws RemoteException {
        super();
        n = _n;
        f = _f;
        pid = _pid;
        port = _port;
    }

    // rmi method, sets ID id this process
    public void mark_start() { 
        try {
            start_block.put(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // RMI method, puts a msg in the queue
    public void set(String type, int round, int value) {
        try {
            switch(type){ 
                case "N": notification_queue.put(new Msg(round, value)); break;
                case "P": proposal_queue.put(new Msg(round, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all PIDs except our own
    public void broadcast(String type, int round, int value) {
        try {
            for (int pid = 0; pid < n; pid++) {
                ProcessInterface destination = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                destination.set(type, round, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] await(BlockingQueue<Msg> queue, int round) {
        int num_expected_msgs = this.n - this.f;
        int[] counts = {0, 0};
        while (num_expected_msgs > 0) {
            try {
                Msg msg = queue.take();
                if(msg.round == round){
                    num_expected_msgs--;
                    if (msg.value == 0 || msg.value == 1) {
                        counts[msg.value]++;
                    }
                }

                if(msg.round > round){
                    queue.put(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return counts;
    }

    // Runnable, contains the main loop of the algorithm
    public void run() {
        try {
            java.rmi.Naming.bind(String.format("rmi://localhost:%d/%d", port, pid), this);  // Registering this process at the registry
            start_block.take();   // wait for start signal
            boolean decided = false;
            int value = ThreadLocalRandom.current().nextInt(0, 2);
            for(int round = 1; ;round++) {
                //System.out.printf("Process %d has value %d\n", pid, value);
                broadcast("N", round, value);

                int[] counts = await(notification_queue, round);
                if (counts[0] > (n + f) / 2) broadcast("P", round, 0);
                else if (counts[1] > (n + f) / 2) broadcast("P", round, 1);
                else broadcast("P", round, -1);

                if (decided) {
                    System.out.printf("Process %d decided %d\n", pid, value);
                    return;
                }

                counts = await(proposal_queue, round);
                if (counts[0] > f) {
                    value = 0;
                    if (counts[0] + counts[1] > 3*f) decided = true;
                }
                else if (counts[1] > f) {
                    value = 1;
                    if (counts[0] + counts[1] > 3*f) decided = true;
                }
            
                else value = ThreadLocalRandom.current().nextInt(0, 2);
            }

            
        } catch (Exception e) {
            System.out.printf("Error running process %d\n" + e.toString(), pid);
            return;
        }
    }
}
