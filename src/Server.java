import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
        
public class Server implements ServerInterface {

	private final int me;
	private final int total;
	private final Map<Integer,Integer> vector;
	
	private final List<Message> queue = new ArrayList<>();
	
    public Server(int me, int total) {
		this.me = me;
		this.total = total;
		this.vector = new HashMap<Integer,Integer>(total);
		for(int i = 0; i < total; i++ ) {
			getVector().put(i, 0);
		}
	}

    public void broadcast(String message) {
    	int v = getVector().get(getMe())+1;
    	getVector().put(getMe(), v);
    	
    	for (int i = 0; i < total; i++) {
    		if (i != this.getMe()) {
    			try {
    				Registry registry = LocateRegistry.getRegistry(Main.PORT);
    				ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(i));
    				System.out.println("broadcasted: " + message);
    				stub.recieve(new Message(message, getVector(), getMe()));
    			} catch (RemoteException | NotBoundException e) {
    				System.err.println("Client exception: " + e.toString());
    				e.printStackTrace();
    			}
    		}
    		
    	}
    }
    
	@Override
	public void recieve(Message message) throws RemoteException {
		if (!canBeDelivered(message)) {
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
		int v = getVector().get(message.getSender())+1;
    	getVector().put(message.getSender(), v);
    	
		queue.remove(message);
		
		System.out.println(String.format("%d Received message from %d with clock %s", me , message.getSender(), message.getVector()));
	}
	
	private boolean canBeDelivered(Message recievedMessage) {
		Map<Integer, Integer> messageVector = recievedMessage.getVector();
		for (Integer key : messageVector.keySet()) {
			int recievedMessageClock = messageVector.get(key);
			Integer ownClock = vector.get(key);
			if (key == recievedMessage.getSender()) {
				ownClock++;
			}
			if (ownClock < recievedMessageClock) {
				return false;
			}
		}
		return true;
	}

	public Map<Integer,Integer> getVector() {
		return vector;
	}

	public int getMe() {
		return me;
	}
}
