import java.rmi.*;
import java.util.concurrent.TimeUnit;
import java.rmi.server.*;
import java.util.concurrent.locks.*;

public class Petersons extends UnicastRemoteObject implements PetersonsInterface, Runnable {
    public int pid, id, port;
    private int tid, ntid, nntid, neighbor_pid;
    boolean is_elected = false, is_active = true;
    BlockingReceive br_ntid = new BlockingReceive();
    BlockingReceive br_nntid = new BlockingReceive(); 
    
    

    public Petersons(int _pid, int num_processes, int _port, int _id) throws RemoteException {
        super();
        pid = _pid;
        id = _id;
        tid = id;
        port = _port;
        neighbor_pid = (pid + 1) % num_processes;
        ntid = -1;
        nntid = -1;
    }

    public int receive_ntid() {
        int result = -1;
        try {
            result = br_ntid.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int receive_nntid() {
        int result = -1;
        try {
            result = br_nntid.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void send_ntid(int ntid) {    
        try {
            PetersonsInterface neighbor = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, neighbor_pid));
            neighbor.set_ntid(ntid);
        } catch (Exception e) { System.out.println(e.toString()); }
    }

    public void send_nntid(int nntid) { 
        try {
            PetersonsInterface neighbor = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, neighbor_pid));
            neighbor.set_nntid(nntid);
        } catch (Exception e) { System.out.println(e.toString()); }
    }

    public void set_ntid(int ntid) {
        // System.out.printf("Setting ntid of process %d\n", pid);
        try {
            br_ntid.set(ntid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set_nntid(int nntid) {
        try {
            br_nntid.set(nntid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test(String message) {
        System.out.printf(message + "%d", this.pid);
    }

    public void run() {
        try {
            java.rmi.Naming.bind(String.format("rmi://localhost:%d/%d", port, pid), this);  // Registering this process at the registry
            TimeUnit.SECONDS.sleep(3);

            PetersonsInterface neighbor = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, neighbor_pid));
            while(true) {
                // System.out.printf("Starting a new round in process %d\n", pid);
                if (is_active) {
                    send_ntid(tid);
                    int ntid = receive_ntid();
                    if (ntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    }
                    send_nntid(Integer.max(ntid, tid));
                    int nntid = receive_nntid();
                    if (nntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    }
                    if (ntid >= tid && ntid >= nntid) {
                        tid = ntid;
                    } else {
                        this.is_active = false;
                    }
                } else {
                    int ntid = receive_ntid();
                    neighbor.set_ntid(ntid);
                    if (ntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    }
                    
                    int nntid = receive_nntid();
                    neighbor.set_nntid(nntid);
                    if (nntid == this.id) {
                        System.out.printf("Process %d with id %d is elected!\n", this.pid, this.id);
                    }
                    
                }
            }
    
        } catch (Exception e) {
            System.out.printf("Error starting process %d\n" + e.toString(), pid);
            return;
        }
    }
}
