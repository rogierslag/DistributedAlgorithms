import java.rmi.Remote;
import java.rmi.RemoteException;


public interface SyncInterface extends Remote,Runnable {
	
	public void nextRound() throws RemoteException;

	public boolean isElected();
	
	public boolean hasDied();
}
