import java.util.*;

//Buffer: keeps a list of msgs in the buffer per process that can send msgs
public class Buffer{
    // list of possible transmitters, each with a list of msgs
    private ArrayList<ArrayList<Msg>> buffer;

    public Buffer(int num_processes){
        buffer = new ArrayList<ArrayList<Msg>>();
        for(int i = 0; i < num_processes; i++){
            // give each process a list
            buffer.add(new ArrayList<Msg>());
        }
    }

    // Implementation of {(m, k, Vm) in B | D_k(m)}
    // aka set of msgs that are now deliverable
    // also removes the deliverable msgs from the buffer.
    public ArrayList<Msg> getDeliverableMsgs(VectorClock local_clock){
        ArrayList<Msg> result = new ArrayList<Msg>();
        
        for(ArrayList<Msg> buf : buffer){
            ListIterator<Msg> iter = buf.listIterator();
            while(iter.hasNext()){
                Msg msg = iter.next();
                if(local_clock.D_j(msg.Vm, msg.source_pid)){
                    result.add(msg);
                    iter.remove();
                }
            }
        }
        return result;
    }

    public void insert(Msg msg){
        buffer.get(msg.source_pid).add(msg);
    }

}