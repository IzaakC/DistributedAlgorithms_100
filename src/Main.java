import java.io.StreamTokenizer;
import java.rmi.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Main{
    public static void main(String[] args) {
        
        // SETTINGS
        int num_processes = 10;
        int port = 5000;
        int min_id = 42;
        int max_id = 123;
        boolean max_id_guaranteed = false; // Determines whether a process gets the max_id as id; used to easily verify correctness
        
        int start_pid, stop_pid;

        // Read command-line argument indicating the total number of processes
        try {
            if(args.length == 2){
                start_pid = Integer.parseInt(args[0]);
                stop_pid = Integer.parseInt(args[1]);  
            } else {
                System.out.println("Using default arguments");
                start_pid = 0;
                stop_pid = num_processes;
            }
        } catch (Exception e) {
            System.out.println("Error parsing arguments: " + e.toString());
            return;
        }
        
        /* Create and install a security manager.
        Note that, contrary to what one would expect, this might also break RMI calls.
        If you are working on a single host, there is no need to use a security manager. */
        if (start_pid > 0 || stop_pid < num_processes) { // Meaning we run distributed
            System.setProperty("java.security.policy", "file:./my.policy");

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
        }
        
        // Start the registry on a given port
        if (start_pid == 0) { // First JVM is responsible for the registry
            try {
                java.rmi.registry.LocateRegistry.createRegistry(port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // Create the processes
        for (int pid = start_pid; pid < stop_pid; pid++) {
            try {
                Petersons p = new Petersons(pid, num_processes, port);
                new Thread(p).start();
            } catch (Exception e) {
                System.out.printf("Error creating process %d\n" + e.toString(), pid);
            }            
        }

        // Wait 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            System.out.println("Error sleeping :(");
        }

        // Only the last JVM should start the processes
        if (stop_pid < num_processes) {
            return;
        }

        // Generate IDs for the processes
        System.out.print("Generated IDs: ");
        Set<Integer> ids = new HashSet<Integer>(num_processes);
        if(max_id_guaranteed) {
            ids.add(max_id);
            System.out.printf("%d, ", max_id);
        }
        while(ids.size() < num_processes) {
            int id = ThreadLocalRandom.current().nextInt(min_id, max_id);
            System.out.printf("%d, ", id);
            ids.add(id);
        }
        System.out.println("");

        // Simulate; start the first round on all processes
        Iterator<Integer> itr = ids.iterator();
        for (int pid = 0; pid < num_processes; pid++) {
            try {
                int id = itr.next();
                PetersonsInterface p = (PetersonsInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                p.set_id(id);
                p.startRound();
            } catch (Exception e) {
                System.out.printf("Error starting process %d\n" + e.toString(), pid);
            }            
        }

    }
}