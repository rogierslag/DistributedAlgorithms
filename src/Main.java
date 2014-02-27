import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main {

	public static final int PORT = 1099;
	
	private static final List<Server> serverList = new ArrayList<>();

	public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(PORT);
            
			int totalNumber = Integer.parseInt(args[0]);
			for(int i = 0; i < totalNumber; i++) {
				createServer(i, totalNumber);
			}
			
			for (Server server : serverList) {
				server.broadcast(Integer.toString(new Random().nextInt()));
			}
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

	private static void createServer(int myNumber, int totalNumber)
			throws RemoteException, AlreadyBoundException, AccessException {
		Server server = new Server(myNumber,totalNumber);
		ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.bind(Integer.toString(myNumber), stub);
		
		serverList.add(server);

		System.out.println("Server" + myNumber + "ready");
	}
}
