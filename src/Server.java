import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
        
public class Server implements ServerInterface {

	public static final String BINDING = "Recieve";
        
    public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(1099);
            Server obj = new Server();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(BINDING, stub);

            System.err.println("Server ready");
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

	@Override
	public void recieve(Message message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliver(Message message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}