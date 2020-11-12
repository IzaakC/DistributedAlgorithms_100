import java.rmi.*;
public class Server{  
    public static void main(String args[]){  
        System.out.println("start!"); 
        try{
            
            ProcessInterface stub = (ProcessInterface) Naming.lookup(String.format("rmi://localhost:5000/%d", 0));
            Process local = new Process(1, 2);
            Msg msg = local.generateMsg("Hi!");
            System.out.println("Sending a msg: ");
            msg.print();
            stub.uponReceptionEvent(msg);
        }catch(Exception e){System.out.println(e);}  
    }  
}  
