import java.rmi.*;
import java.util.concurrent.TimeUnit;

public class Main{
    public static void main(String[] args) {
        
        // SETTINGS
        int n = 15; // Number of processes
        int f = (int) Math.ceil(n/5.0) - 1; // Number of Byzantine processes
        int port = 5000;
        String mode;
        // mode = "regular";    // regular process 
        // mode = "random1";    // random if a msg is sent, with random value
        mode = "random2";    // always sends a msg, but with a random value
        // mode = "fail";       // just empties its queues
        // mode = "deliberately wrong";  // always responds with the opposite value of what a regular process would do
        
        int start_pid, stop_pid;

        // Read command-line argument indicating the total number of processes
        try {
            if(args.length == 2){
                start_pid = Integer.parseInt(args[0]);
                stop_pid = Integer.parseInt(args[1]);  
            } else {
                System.out.println("Using default arguments");
                start_pid = 0;
                stop_pid = n;
            }
        } catch (Exception e) {
            System.out.println("Error parsing arguments: " + e.toString());
            return;
        }
        
        /* Create and install a security manager.
        Note that, contrary to what one would expect, this might also break RMI calls.
        If you are working on a single host, there is no need to use a security manager. */
        if (start_pid > 0 || stop_pid < n) { // Meaning we run distributed
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
                Process p = new Process(pid, n, f, port);
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
        if (stop_pid < n) {
            return;
        }

        // Simulate; start the first round on all processes
        for (int pid = 0; pid < n; pid++) {
            try {
                ProcessInterface p = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:%d/%d", port, pid));
                if (pid < f) p.mark_start(mode); // The first f processes are Byzantines
                else p.mark_start("regular"); // The rest are correct processes

            } catch (Exception e) {
                System.out.printf("Error starting process %d\n" + e.toString(), pid);
            }            
        }
    }
}