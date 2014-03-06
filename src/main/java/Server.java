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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
       
@Getter
public class Server extends Thread implements ServerInterface {
	private final Object $LOCK;
	
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
		this.$LOCK = new Object[0];
		
		for(int i = 0; i < total; i++ ) {
			vector.put(i, 0);
		}
	}

    public void broadcast(String message) {
    	Message message2 = null;
    	synchronized ($LOCK) {
    		incrementClock(getMe());
			message2 = new Message(message, getVector(), getMe());
    	}
	    		
		for (int i = 0; i < total; i++) {
			if (i != this.getMe()) {
				try {
					Registry registry = LocateRegistry.getRegistry(Main.PORT);
					ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(i));
//    				System.out.println("broadcasted: " + message);
					stub.recieve(message2);
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
    	List<Message> syncQueue = queue;
		if (canBeDelivered(message)) {
			deliver(message, syncQueue);
			
			for (Message m : getQueue()) {
				if (canBeDelivered(m)) {
					System.out.println("delivering queued message");
					recieve(m);
				}
			}
		}
		else {
//			System.out.println(String.format("%d Qued message from %d with clock %s", me , message.getSender(), message.getVector()));
			syncQueue.add(message);
		}
	}

	private void deliver(Message message, List<Message> syncQueue) {
		synchronized ($LOCK) {
			incrementClock(message.getSender());
		}
		syncQueue.remove(message);
	}

	private boolean canBeDelivered(Message recievedMessage) {
		Map<Integer, Integer> messageVector = recievedMessage.getVector();
		for (int key : messageVector.keySet()) {
			if (key != getMe()) {
				int recievedMessageClock = messageVector.get(key);
				int ownClock = new HashMap<>(getVector()).get(key);
				if (key == recievedMessage.getSender()) {
					ownClock++;
				}
				if (ownClock < recievedMessageClock) {
					System.err.println("MessageVector: " + recievedMessage.getSender() + ", " + messageVector);
					System.err.println("OwnVector:     " + getMe() + ", " + getVector());
					System.err.println();
					return false;
				}
			}
		}
		return true;
	}
	
	private void incrementClock(int sender) {
		vector.put(sender, getVector().get(sender) + 1);
	}
	
	public ImmutableMap<Integer, Integer> getVector() {
		return ImmutableMap.<Integer, Integer>builder().putAll(vector).build();
	}
	
	public ImmutableList<Message> getQueue() {
		return ImmutableList.<Message>copyOf(queue);
	}
	
	@Override
	public void run() {
		try {
			for (int i = 0; i < numberOfCycles; i++) {
				sleep((long)(Math.random() * 1));
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
