import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
        
public class Server implements ServerInterface {
	
	public static final int PORT = 1099;

	private final int me;
	private final int total;
	private final Map<Integer,Integer> vector;
	
	private final Queue<Message> queue = new PriorityQueue<>();
	
	private static final List<Server> serverList = new ArrayList<>();
        
    public Server(int me, int total) {
		this.me = me;
		this.total = total;
		this.vector = new HashMap<Integer,Integer>(total);
		for(int i = 0; i < total; i++ ) {
			vector.put(i, 0);
		}
	}

	public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(PORT);
            
			int totalNumber = Integer.parseInt(args[0]);
			for(int i = 0; i < totalNumber; i++) {
				createServer(i, totalNumber);
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

    public void broadcast(Message message) {
    	int v = vector.get(me)+1;
    	vector.put(me, v);
    	
    	for ( int i = 0; i < total; i++) {
    		if ( i == this.me) {
    			continue;
    		}
    		try {
    			Registry registry = LocateRegistry.getRegistry(Server.PORT);
    			ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(this.me));
    			stub.recieve(new Message("bla", vector, me));
    		} catch (RemoteException | NotBoundException e) {
    			System.err.println("Client exception: " + e.toString());
    			e.printStackTrace();
    		}
    		
    	}
    }
    
	@Override
	public void recieve(Message message) throws RemoteException {
		if ( !canBeDelivered(message)) {
			queue.add(message);
		} else {
			deliver(message);
			for (Message m : queue) {
				if ( canBeDelivered(m)) {
					deliver(m);
				}
			}
		}
	}

	public void deliver(Message message) {
		int v = vector.get(message.getSender())+1;
    	vector.put(message.getSender(), v);
    	
		queue.remove(message);
		
		System.out.println(String.format("Received message from %d with clock %s",message.getSender(),message.getVector()));
	}
	
	private boolean canBeDelivered(Message m ) {
		for (Integer messageNumer : m.getVector().values()) {
			for (Integer myNumber : vector.values()) {
				if ( messageNumer < myNumber) {
					return false;
				}
			}
		}
		return true;
	}
}
