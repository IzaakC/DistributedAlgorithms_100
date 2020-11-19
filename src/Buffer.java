import java.util.*;

//Buffer: keeps a list of msgs in the buffer per process that can send msgs
public class Buffer{
    // list of possible transmitters, each with a list of msgs
    private ArrayList<Msg> buffer = new ArrayList<Msg>();

    // Implementation of {(m, k, Vm) in B | D_k(m)}
    // aka set of msgs that are now deliverable
    // also removes the deliverable msgs from the buffer.
    public ArrayList<Msg> getDeliverableMsgs(VectorClock local_clock){
        ArrayList<Msg> result = new ArrayList<Msg>();
        
        ListIterator<Msg> iter = buffer.listIterator();
        while(iter.hasNext()){
            Msg msg = iter.next();
            if(local_clock.D_j(msg.Vm, msg.source_pid)){
                result.add(msg);
                iter.remove();
            }
        }
    
        return result;
    }

    public void insert(Msg msg){
        buffer.add(msg);
    }

}