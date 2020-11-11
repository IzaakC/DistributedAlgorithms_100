import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;

public class Process extends UnicastRemoteObject implements ProcessInterface {
    private ArrayList<String> msgs;
    private VectorClock local_clock;
    private Buffer buffer;

    public static int pid;

    public Process(int _pid, int num_processes) throws RemoteException{
        super();
        pid = _pid;
        local_clock = new VectorClock(num_processes);
        buffer = new Buffer(num_processes);

        // todo: establish connections
    }

    public void uponReceipt(Msg msg){
        if(local_clock.D_j(msg.Vm, msg.source_pid)){
            deliver(msg);
            ArrayList<Msg> deliverables = buffer.getDeliverableMsgs(local_clock);
            while(deliverables.size() > 0){
                for(Msg _msg : deliverables)
                    deliver(_msg);
            }
            
        }
        else
            buffer.insert(msg);
    }

    private void deliver(Msg msg){
        msgs.add(msg.content);
        local_clock.increment(msg.source_pid);
    }
}

    
        