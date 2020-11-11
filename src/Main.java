
public class Main{
    public static void main(String[] args) {
        try {
            int pid = Integer.parseInt(args[0]);
            int num_processes = Integer.parseInt(args[0]);
            Process p = new Process(pid, num_processes);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
    }
}