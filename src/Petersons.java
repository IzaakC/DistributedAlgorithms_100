import java.rmi.*;
// import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.*;

public class Petersons extends UnicastRemoteObject implements PetersonsInterface, Runnable {
    public int pid, id, port;
    private int tid, neighbor_pid;
    boolean is_elected = false, is_active = true;
    BlockingQueue<Integer> queue = new LinkedBlockingDeque<Integer>();

    public void startRound(){
        tid = this.id;
        while(true) {
            // System.out.printf("Starting a new round in process %d\n", pid);
            if (is_active) {
                send(tid);
                int ntid = receive();
                if (ntid == this.id) {
                    System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    return;
                }
                send(Integer.max(ntid, tid));
                int nntid = receive();
                if (nntid == this.id) {
                    System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    return;
                }
                if (ntid >= tid && ntid >= nntid) {
                    tid = ntid;
                } else {
                    this.is_active = false;
                }
            } else {
                int ntid = receive();
                send(ntid);
                if (ntid == this.id) {
                    System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    return;
                }
                
                int nntid = receive();
                send(nntid);
                if (nntid == this.id) {
                    System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                }
            }
        }
    }
   
    public Petersons(int _pid, int num_processes, int _port) throws RemoteException {
        super();
        pid = _pid;
        port = _port;
        neighbor_pid = (pid + 1) % num_processes;
    }

    public void set_id(int id) { 
        this.id = id;
    }

    public int receive() {
        int result = -1;
        try {
            result = queue.take();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void send(int var) {    
        try {
            PetersonsInterface neighbor = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, neighbor_pid));
            neighbor.set(var);
        } catch (Exception e) { System.out.println(e.toString()); }
    }

    public void set(int var) {
        try {
            queue.put(var);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            java.rmi.Naming.bind(String.format("rmi://localhost:%d/%d", port, pid), this);  // Registering this process at the registry    
        } catch (Exception e) {
            System.out.printf("Error starting process %d\n" + e.toString(), pid);
            return;
        }
    }
}
