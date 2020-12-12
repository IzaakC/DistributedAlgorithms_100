import java.rmi.*;
// import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;

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

    // rmi method, mark start of this process
    public void mark_start() { 
        try {
            start_block.put(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // RMI method, puts a msg in the queue with a random delay
    public void set(String type, int round, int value) {
        Timer timer = new Timer();
        int delay = ThreadLocalRandom.current().nextInt(0, 1000 + 1);
        timer.schedule(new uponReceptionEvent(type, round, value, timer), delay);
    }

    // Broadcast message to all PIDs except our own
    public void broadcast(String type, int round, int value) {
        try {
            for (int pid = 0; pid < n; pid++) {
                if (pid == this.pid) continue;
                ProcessInterface destination = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                destination.set(type, round, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TimerTask that executes the uponReception method (run()) with a delay
    class uponReceptionEvent extends TimerTask {
        private String type;      
        private int round, value;
        private Timer timer; 
        
        public uponReceptionEvent(String type, int round, int value, Timer timer){
            this.type = type;
            this.round = round;
            this.value = value;
            this.timer = timer;
        }
   
        public void run(){
            try {
                switch(type){ 
                    case "N": notification_queue.put(new Msg(round, value)); break;
                    case "P": proposal_queue.put(new Msg(round, value));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            timer.cancel(); // stop the timer
        }
    }

    // Wait for n-f msgs of the expected type
    public int[] await(BlockingQueue<Msg> queue, int round) {
        int num_expected_msgs = n - f;
        int[] counts = {0, 0};
        while (num_expected_msgs > 0) {
            try {
                Msg msg = queue.take();
                if (msg.round == round){
                    num_expected_msgs--;
                    if (msg.value == 0 || msg.value == 1) {
                        counts[msg.value]++;
                    }
                }

                // due to delays and not expecting n msgs it can be possible to receive a msg from a future round (I think)
                if (msg.round > round) queue.put(msg); // save it for later

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
                    System.out.printf("Process %d decided %d in round %d\n", pid, value, round);
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
