import java.rmi.Remote;
import java.rmi.RemoteException;


public interface OrdinaryInterface extends SyncInterface {

	public void check(PossibleResponse data) throws RemoteException;
	
}
