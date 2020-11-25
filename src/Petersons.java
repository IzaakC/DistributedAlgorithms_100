import java.rmi.*;
// import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.*;

public class Petersons extends UnicastRemoteObject implements PetersonsInterface, Runnable {
    public int pid, id, port;
    private int tid, neighbor_pid;
    boolean is_elected = false, is_active = true;
    BlockingQueue<Integer> queue = new LinkedBlockingDeque<Integer>();
    BlockingQueue<Integer> id_block = new LinkedBlockingDeque<Integer>();
    
    // constructor, save pid, port and calculate the neighbor id
    public Petersons(int _pid, int num_processes, int _port) throws RemoteException {
        super();
        pid = _pid;
        port = _port;
        neighbor_pid = (pid + 1) % num_processes;
    }

    // rmi method, sets ID id this process
    public void set_id(int id) { 
        try {
            id_block.put(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // blocking receive
    public int receive() {
        int result = -1;
        try {
            result = queue.take();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Sends the var to the neighbor by calling set()
    public void send(int var) {    
        try {
            PetersonsInterface neighbor = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, neighbor_pid));
            neighbor.set(var);
        } catch (Exception e) { System.out.println(e.toString()); }
    }

    // RMI method, puts a variable in the queue
    public void set(int var) {
        try {
            queue.put(var);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Runnable, contains the main loop of the algorithm
    public void run() {
        try {
            java.rmi.Naming.bind(String.format("rmi://localhost:%d/%d", port, pid), this);  // Registering this process at the registry
            id = id_block.take();   // wait for start signal
            tid = id;               // copy id to tid
    
            while(true) {
                /* ----------- ACTIVE ----------*/
                if (is_active) {
                    // 1) Send and receive tid
                    send(tid);
                    int ntid = receive();
                    if (ntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                        return;
                    }

                    //  2) Send and receive max(tid, ntid)
                    send(Integer.max(ntid, tid));
                    int nntid = receive();
                    if (nntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                        return;
                    }

                    // 3) Compare and decide whether or not to turn passive
                    if (ntid >= tid && ntid >= nntid) {
                        tid = ntid;
                    } else {
                        this.is_active = false;
                    }

                /* ----------- PASSIVE ----------*/
                } else {
                    // Send and relay ntid msg
                    int ntid = receive();
                    send(ntid);
                    if (ntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                        return;
                    }
                    
                    // Send and relay nntid msg
                    int nntid = receive();
                    send(nntid);
                    if (nntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.printf("Error running process %d\n" + e.toString(), pid);
            return;
        }
    }
}
