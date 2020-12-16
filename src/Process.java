import java.rmi.*;
// import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;

public class Process extends UnicastRemoteObject implements ProcessInterface, Runnable {
    public int pid, port, n, f, initial_value;
    BlockingQueue<Msg> notification_queue = new LinkedBlockingDeque<Msg>(); // Queue for N messages
    BlockingQueue<Msg> proposal_queue = new LinkedBlockingDeque<Msg>(); // Queue for P messages
    BlockingQueue<String> start_block = new LinkedBlockingDeque<String>();
    private static int max_delay = 100;
    
    // constructor, save pid, port and calculate the neighbor id
    public Process(int _pid, int _n, int _f, int _port, int _initial_value) throws RemoteException {
        super();
        n = _n;
        f = _f;
        pid = _pid;
        port = _port;
        initial_value = _initial_value;
    }

    // RMI method, mark start of this process
    public void mark_start(String mode) { 
        try {
            start_block.put(mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // RMI method, puts a msg in the queue with a random delay
    public void set(String type, int round, int value) {
        Timer timer = new Timer();
        int delay = ThreadLocalRandom.current().nextInt(0, max_delay + 1);
        timer.schedule(new uponReceptionEvent(type, round, value, timer), delay);
    }

    // Broadcast message to all, including self
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

    // Send message to destination
    public void send(String type, int round, int value, int pid) {
        try {
            ProcessInterface destination = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
            destination.set(type, round, value);
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
        int[] counts = {0, 0}; // Index corresponds to value, so counts[1] is the number of messages of form (*, r, 1)
        while (num_expected_msgs > 0) {
            // System.out.printf("Process %d expects %d more messages\n", pid, num_expected_msgs);
            try {
                Msg msg = queue.take();
                if (msg.round == round){
                    num_expected_msgs--;
                    if (msg.value == 0 || msg.value == 1) {
                        counts[msg.value]++;
                    }
                }

                // Due to delays and not expecting n msgs it can be possible to receive a msg from a future round (I think)
                if (msg.round > round) queue.put(msg); // Save it for later

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
            String mode = start_block.take();   // wait for start signal
            
            // System.out.printf("Process %d starting in mode %s\n", pid, mode);
            
            switch(mode) {
                case "regular"              : regularProcess(); break;
                case "fail"                 : spin(); break;
                case "random"               : byzantineRandom(); break;
                case "always send"          : byzantineRandomAlwaysSend(); break;
                case "deliberately wrong"   : byzantineDeliberatelyWrong(); break;
                case "different msgs"       : byzantineDifferentMsgs(); break;
                case "fake rounds"          : byzantineFakeRound(); break;
                default: regularProcess();
            }
            
        } catch (Exception e) {
            System.out.printf("Error running process %d\n" + e.toString(), pid);
            return;
        }
    }

    public void regularProcess(){
        boolean decided = false;
        int value = this.initial_value;
        System.out.printf("Initial value of correct process %d: %d\n", pid, initial_value);
        for(int round = 1; ;round++) {
            /*------- NOTIFICATION PHASE -------------------- */

            // broadcast current value
            broadcast("N", round, value);

            // wait for n - f responses of the current round
            int[] counts = await(notification_queue, round);
            
            /* ------- PROPOSAL PHASE ------------------------*/
            // broadcast proposal depending on the number of msgs of each type
            if (counts[0] > (n + f) / 2) broadcast("P", round, 0);
            else if (counts[1] > (n + f) / 2) broadcast("P", round, 1);
            else broadcast("P", round, -1);
            
            if (decided) {
                System.out.printf("Correct process %d decided %d in round %d\n", pid, value, round-1);
                return;
            }

            counts = await(proposal_queue, round);
            
            /* ------- DECISION PHASE ------------------------*/

            // check if we have enough votes for a decision
            if (counts[0] > f) {
                value = 0;
                if (counts[0] + counts[1] > 3*f) decided = true;
            }
            else if (counts[1] > f) {
                value = 1;
                if (counts[0] + counts[1] > 3*f) decided = true;
            }
            else {
                value = ThreadLocalRandom.current().nextInt(0, 2); // else pick a random value
                System.out.printf("Correct process %d picked value %d when it couldn't decide in round %d\n", pid, value, round);
            }
        }
    }


    // Just empties the queue, does not respond
    public void spin(){
        while(true){
            try {
                notification_queue.take();
                proposal_queue.take();
            } catch (Exception e) {
                System.out.printf("Error running process %d\n" + e.toString(), pid);
            }
        }
    }

    // Random if it broadcasts, sends a random value if it does (same to all)
    public void byzantineRandom(){
        for(int round = 1; ;round++) {
            
            if (ThreadLocalRandom.current().nextInt(0, 1) == 0) {
                broadcast("N", round, ThreadLocalRandom.current().nextInt(0, 2));
                }
            await(notification_queue, round);

            if (ThreadLocalRandom.current().nextInt(0, 1) == 0) {
                broadcast("P", round, ThreadLocalRandom.current().nextInt(-1, 2));                
            }
            await(proposal_queue, round);
        }
    }

    // Always broadcasts, but with a random value (same to all)
    public void byzantineRandomAlwaysSend(){
        for (int round = 1; ;round++) {
            broadcast("N", round, ThreadLocalRandom.current().nextInt(0, 2));
            await(notification_queue, round);
            broadcast("P", round, ThreadLocalRandom.current().nextInt(-1, 2));       
            await(proposal_queue, round);
        }
    }
    
    // Always sends the wrong broadcast, opposite of what it's supposed to send
    public void byzantineDeliberatelyWrong(){
        int value = ThreadLocalRandom.current().nextInt(0, 2);
        for(int round = 1; ;round++) {
            broadcast("N", round, value);

            int[] counts = await(notification_queue, round);
            if (counts[1] > (n + f) / 2) broadcast("P", round, 0);
            else if (counts[0] > (n + f) / 2) broadcast("P", round, 1);
            else broadcast("P", round, -1);

            counts = await(proposal_queue, round);
            if (counts[1] > f) value = 0;
            else if (counts[0] > f) value = 1;
            else value = ThreadLocalRandom.current().nextInt(0, 2);
        }
    }

    // Sends different (random) msgs to all nodes
    public void byzantineDifferentMsgs(){
        int value = ThreadLocalRandom.current().nextInt(0, 2);
        for(int round = 1; ;round++) {
            for(int pid = 0; pid < n; pid++){
                value = ThreadLocalRandom.current().nextInt(0, 2);
                send("N", round, value, pid);
            }
            await(notification_queue, round);

            for(int pid = 0; pid < n; pid++){
                value = ThreadLocalRandom.current().nextInt(0, 2);
                send("P", round, value, pid);
            }
            await(proposal_queue, round);
        }
    }

    // Fakes its round number as if it is in the future
    public void byzantineFakeRound(){
        int fake_round = 5;
        for(int round = 1; ;round++) {
            if(round == fake_round)
                fake_round += 5;
            
            for(int pid = 0; pid < n; pid++)
                send("N", fake_round, 1, pid);
            await(notification_queue, round);

            for(int pid = 0; pid < n; pid++)
                send("P", fake_round, 1, pid);
            await(proposal_queue, round);
        }
    }
}
