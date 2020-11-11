import java.rmi.*;

public interface ProcessInterface extends Remote{
    public void uponReceptionEvent(Msg msg) throws RemoteException; 
}