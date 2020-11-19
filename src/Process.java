import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class Process extends UnicastRemoteObject implements ProcessInterface, Runnable {
    private ArrayList<String> msgs;
    private VectorClock local_clock;
    private Buffer buffer;
    private int num_processes;
    public int pid;


    public Process(int _pid, int _num_processes) throws RemoteException{
        super();
        pid = _pid;
        local_clock = new VectorClock(_num_processes);
        buffer = new Buffer();
        msgs = new ArrayList<String>();
        num_processes = _num_processes;
    }

    public void broadcast(String content){
        local_clock.increment(pid);
        Msg msg = new Msg(content, local_clock, pid);

        for(int pid = 0; pid < num_processes; pid++){
            if(pid == this.pid) continue;

            try {
                // find the remote instance of process pid
                ProcessInterface p = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:5000/%d", pid));
                
                // and put the msg in the channel
                p.putMsgInChannel(msg);
            } catch (Exception e) {
                System.out.printf("Error broadcasting by process %d to pid %d\n", this.pid, pid);
                System.out.println(e);
            }
        }
    }

    // called by a remote JVM, puts a msg in the channel. The msg is delayed by a random delay (max 10 seconds)
    public void putMsgInChannel(Msg msg){
        Timer timer = new Timer();
        int delay = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
        timer.schedule(new uponReceptionEvent(msg, timer), delay);
    }

    // TimerTask that executes the uponReception method (run()) with a delay
    class uponReceptionEvent extends TimerTask {
        private Msg msg;        // msg to be delivered
        private Timer timer;    // timer that calls run() with some delay
        
        public uponReceptionEvent(Msg _msg, Timer _timer){
            timer = _timer;
            msg = _msg;
        }
   
        public synchronized void run(){
            // check if the msg is deliverable (if this process is up-to-date according to the sender)
            synchronized(Process.this){
                if(local_clock.D_j(msg.Vm, msg.source_pid)){
                    deliver(msg);

                    // deliver msgs that have become available by delivering the new msg.
                    ArrayList<Msg> deliverables = buffer.getDeliverableMsgs(local_clock);
                    while(deliverables.size() > 0){
                        for(Msg _msg : deliverables)
                            deliver(_msg);
                        
                        // check for new deliverable msgs
                        deliverables = buffer.getDeliverableMsgs(local_clock);
                    }
                    
                }
                else // can not deliver yet, put it in the buffer
                    buffer.insert(msg);
            }
        
            timer.cancel(); // stop the timer
        }
    }

    private void deliver(Msg msg){
        System.out.printf("Process %d Received a msg:\n", pid);
        msg.print("\t");

        System.out.printf("\tLocal clock: ");        
        local_clock.print();
        msgs.add(msg.content);
        local_clock.increment(msg.source_pid);
    }

    public void printMsgs() {
        String msgsString = String.format("Inbox of Process %d: ", pid);
        for (String msg : this.msgs) {
            msgsString += msg + ", ";
        }
        System.out.println(msgsString);
    }

    public void run() {
        try {
            java.rmi.Naming.bind(String.format("rmi://localhost:5000/%d", pid), this);  // Registering this process at the registry 
        } catch (Exception e) {
            System.out.printf("Error starting process %d\n" + e.toString(), pid);
            return;
        }
    }
}    
        