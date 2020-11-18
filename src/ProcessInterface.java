import java.rmi.*;

public interface ProcessInterface extends Remote{
    public void uponReceptionEvent(Msg msg) throws RemoteException; 
    public void connect() throws RemoteException;
    public void broadcast(String content) throws RemoteException;
}