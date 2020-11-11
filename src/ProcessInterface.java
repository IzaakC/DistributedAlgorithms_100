import java.rmi.*;

public interface ProcessInterface extends Remote{
    public void uponReceipt(Msg msg) throws RemoteException; 
}