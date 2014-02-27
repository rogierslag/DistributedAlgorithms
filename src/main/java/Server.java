import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.Synchronized;
       
@Getter
public class Server extends Thread implements ServerInterface {

	private final int me;
	private final int total;
	private final Map<Integer, Integer> vector;
	private final int numberOfCycles;
	
	private final List<Message> queue = new CopyOnWriteArrayList<Message>();
	
    public Server(int me, int total, int numberOfCycles) {
		this.me = me;
		this.total = total;
		this.vector = new HashMap<Integer, Integer>(total);
		this.numberOfCycles = numberOfCycles;
		
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
//    				System.out.println("broadcasted: " + message);
    				stub.recieve(new Message(message, getVector(), getMe()));
    			}
    			catch (RemoteException | NotBoundException e) {
    				System.err.println("Client exception: " + e.toString());
    				e.printStackTrace();
    			}
    		}
    		
    	}
    }
    
	@Override
	@Synchronized
	public void recieve(Message message) throws RemoteException {
		if (!canBeDelivered(message)) {
//			System.out.println(String.format("%d, %d Qued message from with clock %s", me , message.getSender(), message.getVector()));
			queue.add(message);
		}
		else {
			deliver(message);
			for (Message m : queue) {
				if (canBeDelivered(m)) {
					deliver(m);
				}
			}
		}
	}

	public void deliver(Message message) {
		int v = getVector().get(message.getSender())+1;
    	getVector().put(message.getSender(), v);
    	
		queue.remove(message);
		
//		System.out.println(String.format("%d, %d Received message from with clock %s", me , message.getSender(), message.getVector()));
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
				System.err.println(me +", " + key + ": " + ownClock + ",  " + recievedMessageClock);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			for (int i = 0; i < numberOfCycles; i++) {
				sleep((long)(Math.random() * 1000));
				broadcast(Integer.toString(new Random().nextInt()));
			}
			Main.checkThreads();
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
