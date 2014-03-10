import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CandidateInterface extends SyncInterface {
	
	public void ack(Ordinary ordinary) throws RemoteException;

}
