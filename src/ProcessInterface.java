import java.rmi.*;

public interface ProcessInterface extends Remote {
    public void putMsgInChannel(Msg msg) throws RemoteException;
    public void broadcast(String content) throws RemoteException;
    public void printMsgs() throws RemoteException;
}