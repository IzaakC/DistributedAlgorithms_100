import java.rmi.*;

public interface ProcessInterface extends Remote {
    public void set(String type, int round, int value) throws RemoteException;
    public void mark_start() throws RemoteException;
}