import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Recieve extends Remote {
    void recieve(Message message) throws RemoteException;
}