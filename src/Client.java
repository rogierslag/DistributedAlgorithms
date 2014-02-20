import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Vector;

public class Client {
	
	private final ArrayList<Integer> vector = new ArrayList<>();
	
	public static void main(String[] args) {
		String host = (args.length < 1) ? null : args[0];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			Recieve stub = (Recieve) registry.lookup(Server.BINDING);
//			stub.recieve(new Message("bla", vector));
		} catch (RemoteException | NotBoundException e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}