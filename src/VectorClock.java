import java.util.ArrayList;
import java.io.*;

public class VectorClock implements Serializable{
    // the vector clock, indexed by process id (int)
    public ArrayList<Integer> clock;

    // constructor, initializes all clock values to zero
    public VectorClock(int num_processes){
        this.clock = new ArrayList<Integer>();
        for(int i = 0; i < num_processes; i++)
            clock.add(0);
    }

    // compare function to check if V + e_j >= V_m
    boolean D_j(VectorClock Vm, int source_pid){
        
        if(this.clock.size() != Vm.clock.size()){
            System.out.println("Vector clocks not equal in length..");
            return false;
        }
        
        boolean result = true;
        increment(source_pid);
        
        for(int i = 0; i < clock.size(); i++){
            if( this.clock.get(i) < Vm.clock.get(i)){
                result = false;
                break;
            }
        }

        decrement(source_pid);
        return result;
    }

    public void increment(int i){
        clock.set(i, clock.get(i) + 1);
    }

    public void decrement(int i){
        clock.set(i, clock.get(i) - 1);
    }

    public void print(){
        for(Integer c : clock)
            System.out.printf(" %d", c);
        System.out.printf("\n");
    }
}   