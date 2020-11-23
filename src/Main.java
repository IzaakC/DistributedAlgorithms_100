
import java.io.StreamTokenizer;
import java.rmi.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main{
    public static void main(String[] args) {
        
        // SETTINGS
        int num_processes = 3;
        int port = 5000;
        
        int start_id, stop_id;

        // Read command-line argument indicating the total number of processes
        try {
            if(args.length == 2){
                start_id = Integer.parseInt(args[0]);
                stop_id = Integer.parseInt(args[1]);  
            } else {
                System.out.println("Using default arguments");
                start_id = 0;
                stop_id = num_processes;
            }
        } catch (Exception e) {
            System.out.println("Error parsing arguments: " + e.toString());
            return;
        }
        
        /* Create and install a security manager.
        Note that, contrary to what one would expect, this might also break RMI calls.
        If you are working on a single host, there is no need to use a security manager. */
        if (start_id > 0 || stop_id < num_processes) { // Meaning we run distributed
            System.setProperty("java.security.policy", "file:./my.policy");

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
        }
        
        // Start the registry on a given port
        if (start_id == 0) { // First JVM is responsible for the registry
            try {
                java.rmi.registry.LocateRegistry.createRegistry(port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // Start the processes
        for (int pid = start_id; pid < stop_id; pid++) {
            try {
                Process p = new Process(pid, num_processes, port);
                new Thread(p).start();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }

        // Run simulation only on the last JVM
        if (stop_id < num_processes) {
            System.out.println("Processes starting; now enterting infinite while...");
            return;
        }

        // Sleep for 5 seconds because the processes might not
        try {
            TimeUnit.SECONDS.sleep(11);
        } catch (Exception e) {
            System.out.println("Error sleeping :( " + e.toString());
        }

        // Simulate; randomly pick a process that broadcasts a message containing an incremented global counter and repeat
        for(int i = 0; i < 123; i++) {
            int pid = ThreadLocalRandom.current().nextInt(0, num_processes);
            try {
                ProcessInterface p = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                p.broadcast(String.format("%d", i));
            } catch (Exception e) {
                System.out.printf("Error broadcasting by process %d\n", pid);
                System.out.println(e);
            }
        }
        
        // Sleep for 11 seconds because msgs can be delayed by 10 seconds
        try {
            TimeUnit.SECONDS.sleep(11);
        } catch (Exception e) {
            System.out.println("Error sleeping :(");
            System.out.println(e);
        }

        // Print the inboxes of the processes
        for(int pid = 0; pid < num_processes; pid++) {
            try {
                ProcessInterface p = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                p.printMsgs();
            } catch (Exception e) {
                System.out.printf("Error printing by process %d\n" + e.toString(), pid);
            }
        }

    }
}