import java.rmi.*;

public interface ProcessInterface extends Remote{
    public void uponReceiptionEvent(Msg msg) throws RemoteException; 
}