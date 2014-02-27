import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.Getter;
       
@Getter
public class Server extends Thread implements ServerInterface {

	private final int me;
	private final int total;
	private final Map<Integer, Integer> vector;
	private final int numberOfCycles;
	
	private final List<Message> queue = new ArrayList<>();
	
    public Server(int me, int total, int numberOfCycles) {
		this.me = me;
		this.total = total;
		this.vector = Collections.synchronizedMap(new HashMap<Integer, Integer>(total));
		this.numberOfCycles = numberOfCycles;
		
		for(int i = 0; i < total; i++ ) {
			getVector().put(i, 0);
		}
	}

    public void broadcast(String message) {
    	incrementClock(getMe());
    	
    	for (int i = 0; i < total; i++) {
    		if (i != this.getMe()) {
    			try {
    				Registry registry = LocateRegistry.getRegistry(Main.PORT);
    				ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(i));
//    				System.out.println("broadcasted: " + message);
    				stub.recieve(new Message(message, new HashMap<>(getVector()), getMe()));
    			}
    			catch (RemoteException | NotBoundException e) {
    				System.err.println("Client exception: " + e.toString());
    				e.printStackTrace();
    			}
    		}
    		
    	}
    }
    
	@Override
	public void recieve(Message message) throws RemoteException {
		List<Message> copyQueue = new ArrayList<>(queue);
		if (canBeDelivered(message)) {
			deliver(message, queue);
		}
		else {
			System.out.println(String.format("%d, %d Qued message from with clock %s", me , message.getSender(), message.getVector()));
			if (!queue.contains(message)) {
				queue.add(message);
			}
		}
		
		for (Message m : copyQueue) {
			if (canBeDelivered(m)) {
				System.out.println("delivering queued message");
				recieve(m);
			}
		}
	}

	public void deliver(Message message, List<Message> copyQueue) {
    	incrementClock(message.getSender());
    	
    	copyQueue.remove(message);

//		System.out.println(String.format("%d, %d Received message from with clock %s", me , message.getSender(), message.getVector()));
	}
	
	private boolean canBeDelivered(Message recievedMessage) {
		Map<Integer, Integer> messageVector = recievedMessage.getVector();
		for (int key : messageVector.keySet()) {
			int recievedMessageClock = messageVector.get(key);
			int ownClock = getVector().get(key);
			if (key == recievedMessage.getSender()) {
				ownClock++;
			}
			if (ownClock < recievedMessageClock) {
//				System.err.println(me +", " + key + ": " + ownClock + ",  " + recievedMessageClock);
				return false;
			}
		}
		return true;
	}
	
	private void incrementClock(int sender) {
		getVector().put(sender, getVector().get(sender) + 1);
	}
	
	@Override
	public void run() {
		try {
			for (int i = 0; i < numberOfCycles; i++) {
				sleep((long)(Math.random() * 100));
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
