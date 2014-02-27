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
	public static final int NUMBER_OF_CYCLES = 3;
	
	private static final List<Server> serverList = new ArrayList<>();

	public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(PORT);
            
			int totalNumber = Integer.parseInt(args[0]);
			for(int i = 0; i < totalNumber; i++) {
				createServer(i, totalNumber);
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
	
	public static void checkThreads() {
		for (Server server : serverList) {
			List<Message> que = server.getQueue();
			System.err.println("size " + server.getMe() + ": " + que.size());
//			System.err.println(server.getVector());
//			for (Message message : que) {
//				System.err.println(message.getVector());
//			}
		}
//		System.err.println("done");
	}

	private static void createServer(int myNumber, int totalNumber)
			throws RemoteException, AlreadyBoundException, AccessException {
		Server server = new Server(myNumber,totalNumber, NUMBER_OF_CYCLES);
		ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);

		// Bind the remote object's stub in the registry
		Registry registry = LocateRegistry.getRegistry();
		registry.bind(Integer.toString(myNumber), stub);
		
		serverList.add(server);

		System.out.println("Server" + myNumber + "ready");
	}
}
