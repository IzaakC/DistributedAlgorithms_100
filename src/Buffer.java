import java.util.*;

public class Buffer{
    private ArrayList<ArrayList<Msg>> buffer;

    public Buffer(int num_processes){
        for(int i = 0; i < num_processes; i++){
            buffer.add(new ArrayList<Msg>());
        }
    }

    // Implementation of {(m, k, Vm) in B | D_k(m)}
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