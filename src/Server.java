import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Queue;
        
public class Server implements ServerInterface {

	private final int me;
	private final int total;
	private Map<Integer,Integer> vector;
	
	public static final int PORT = 1099;
	
	private final Queue<Message> queue = new PriorityQueue<>();
        
    public Server(int parseInt, int parseInt2) {
		this.me = parseInt;
		this.total = parseInt2;
		this.vector = new HashMap<Integer,Integer>(total);
		for(int i = 0; i < total; i++ ) {
			vector.put(i, 0);
		}
	}

	public static void main(String args[]) {
    	
        try {
        	java.rmi.registry.LocateRegistry.createRegistry(1099);
            Server obj = new Server(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);

            System.err.println("Server ready");
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
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