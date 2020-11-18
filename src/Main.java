
import java.rmi.*;

public class Main{
    public static void main(String[] args) {
        try {
            int pid = Integer.parseInt(args[0]);
            int num_processes = Integer.parseInt(args[1]);
            Process p = new Process(pid, num_processes);
            Naming.rebind(String.format("rmi://localhost:5000/%d", pid), p);  

        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }
}