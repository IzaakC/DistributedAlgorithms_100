import java.rmi.*;
import java.util.ArrayList;

public class Server{  
    private static ArrayList<ProcessInterface> stubs;

    public static void main(String args[]) {
        System.out.println("start!");
        stubs = new ArrayList<ProcessInterface>();
        try {
            int num_processes = Integer.parseInt(args[0]);

            // connect to all
            for (int i = 0; i < num_processes; i++) {
                stubs.add((ProcessInterface) Naming.lookup(String.format("rmi://localhost:5000/%d", i)));
            }

            // tell them all to connect
            for (ProcessInterface stub : stubs) {
                stub.connect();
            }

            stubs.get(0).broadcast("bcm from 0!");
            stubs.get(1).broadcast("bcm from 1!");

        }catch(Exception e){System.out.println(e);}  
    }  
}  
