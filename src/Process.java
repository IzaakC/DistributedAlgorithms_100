import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

// try{  
//     Adder stub=(Adder)Naming.lookup("rmi://localhost:5000/sonoo");  
//     System.out.println(stub.add(34,4));  
//     }catch(Exception e){}  

public class Process extends UnicastRemoteObject implements ProcessInterface {
    private ArrayList<String> msgs;
    private VectorClock local_clock;
    private Buffer buffer;
    private ArrayList<ProcessInterface> stubs;
    private int num_processes;
    public static int pid;


    public Process(int _pid, int _num_processes) throws RemoteException{
        super();
        pid = _pid;
        local_clock = new VectorClock(_num_processes);
        buffer = new Buffer(_num_processes);
        msgs = new ArrayList<String>();
        num_processes = _num_processes;
    }

    public void connect(){
        stubs = new ArrayList<ProcessInterface>();
        for(int i = 0; i < num_processes; i++){
            if(i != pid){
                try {
                    stubs.add((ProcessInterface) Naming.lookup(String.format("rmi://localhost:5000/%d", i)));
                } catch (Exception e) {
                    System.out.println(e);
                }          
            }
        }
    }

    public void broadcast(String content){
        Msg msg = generateMsg(content);
        for(ProcessInterface stub : stubs){
            try {
                stub.uponReceptionEvent(msg);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    // upon reception event: called by a remote JVM
    public void uponReceptionEvent(Msg msg){
        System.out.println("Received a msg: ");
        msg.print();

        System.out.printf("Local clock: ");
        local_clock.print();

        // check if the msg is deliverable (if this process is up-to-date according to the sender)
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

    private void deliver(Msg msg){
        System.out.printf("Process %d Received a msg: '%s'\n", pid, msg.content);
        msgs.add(msg.content);
        local_clock.increment(msg.source_pid);
    }

    public Msg generateMsg(String content){
        local_clock.increment(pid);
        return new Msg(content, local_clock, pid);
    }
}

    
        