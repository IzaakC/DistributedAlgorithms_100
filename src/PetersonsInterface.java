import java.rmi.*;

public interface PetersonsInterface extends Remote {
    public void set(int var) throws RemoteException;
    public void startRound() throws RemoteException;
    public void set_id(int id) throws RemoteException;
}