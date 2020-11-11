import java.util.ArrayList;

public class VectorClock{
    // the vector clock, indexed by process id (int)
    public ArrayList<Integer> clock;

    // constructor, initializes all clock values to zero
    public VectorClock(int num_processes){
        for(int i = 0; i < num_processes; i++)
            clock.add(0);
    }

    // compare function to check if "this" >= other
    boolean D_j(VectorClock other, int source_pid){
        
        if(this.clock.size() != other.clock.size()){
            System.out.println("Vector clocks not equal in length..");
            return false;
        }   
        
        boolean result = true;
        increment(source_pid);
        
        for(int i = 0; i < clock.size(); i++){
            if( this.clock.get(i) < other.clock.get(i)){
                result = false;
                break;
            }
        }

        decrement(source_pid);
        return result;
    }

    void increment(int i){
        clock.set(i, clock.get(i) + 1);
    }


    void decrement(int i){
        clock.set(i, clock.get(i) - 1);
    }
}   