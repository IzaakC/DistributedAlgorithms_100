import java.rmi.*;

public interface ProcessInterface extends Remote {
    public void set(int var) throws RemoteException;
    public void set_id(int id) throws RemoteException;
}