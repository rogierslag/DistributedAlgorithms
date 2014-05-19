import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static final int PORT = 1099;

	private static final List<Server> serverList = new ArrayList<>();

	public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(PORT);
			int totalNumber = Integer.parseInt(args[0]);
	        int traitors = Integer.parseInt(args[1]);
//	        if ( traitors >= (totalNumber+0.0)/5) {
//		        System.err.println("Can't deal with this!");
//		        System.exit(2);
//	        }
			for(int i = 0; i < totalNumber; i++) {
				createServer(i, totalNumber, traitors);
			}
			
			for (Server server : serverList) {
				server.start();
			}
        }
        catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
	
	private static void createServer(int myNumber, int totalNumber, int traitors)
			throws RemoteException, AlreadyBoundException, AccessException {
		Server server = new Server(myNumber,totalNumber,traitors);
		ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.bind(Integer.toString(myNumber), stub);
		
		serverList.add(server);

		System.out.println("Server " + myNumber + " ready");
	}
}
