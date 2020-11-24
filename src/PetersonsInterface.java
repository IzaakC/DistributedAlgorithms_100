import java.rmi.*;

public interface PetersonsInterface extends Remote {
    public void set_ntid(int ntid) throws RemoteException;
    public void set_nntid(int nntid) throws RemoteException;
    public void test(String message) throws RemoteException;
}