import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;

import com.google.common.collect.ImmutableList;
       
@Getter
public class Server extends Thread implements ServerInterface {
	private final int me;
	private final int total;
	private final List<Integer> vector;
	private final int numberOfCycles;
	
	private final List<Message> queue = new CopyOnWriteArrayList<>();
	
    public Server(int me, int total, int numberOfCycles) {
		this.me = me;
		this.total = total;
		this.vector = new CopyOnWriteArrayList<>();
		this.numberOfCycles = numberOfCycles;
		
		for(int i = 0; i < total; i++ ) {
			vector.add(0);
		}
	}

    public void broadcast(String message) {
    	Message message2 = null;
    	synchronized (this) {
    		message2 = new Message(message, incrementClock(getMe()), getMe());
    	}
	    		
		for (int i = 0; i < total; i++) {
			if (i != this.getMe()) {
				try {
					Registry registry = LocateRegistry.getRegistry(Main.PORT);
					ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(i));
//    				System.out.println("broadcasted: " + message);
					stub.recieve(message2, false);
				}
				catch (RemoteException | NotBoundException e) {
					System.err.println("Client exception: " + e.toString());
					e.printStackTrace();
				}
			}
		}
    }
    
	@Override
	public synchronized void recieve(Message message, boolean recursively) throws RemoteException {
		List<Message> syncQueue = queue;
		if (canBeDelivered(message)) {
			deliver(message, syncQueue);
			
			for (Message m : getQueue()) {
				if (canBeDelivered(m)) {
					System.out.println("delivering queued message");
					recieve(m, true);
				}
			}
		}
		else if (!recursively) {
//			System.out.println(String.format("%d Qued message from %d with clock %s", me , message.getSender(), message.getVector()));
			syncQueue.add(message);
		}
	}

	private void deliver(Message message, List<Message> syncQueue) {
		incrementClock(message.getSender());
		syncQueue.remove(message);
	}

	private boolean canBeDelivered(Message recievedMessage) {
		List<Integer> messageVector = recievedMessage.getVector();
		for(int i = 0; i < total; i++ ) {
			if (i != getMe()) {
				int recievedMessageClock = messageVector.get(i);
				int ownClock = vector.get(i);
				if (i == recievedMessage.getSender()) {
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
	
	private synchronized List<Integer> incrementClock(int sender) {
		vector.set(sender, vector.get(sender) + 1);
		return vector;
	}
	
	public List<Integer> getVector() {
		return vector;
	}
	
	public ImmutableList<Message> getQueue() {
		return ImmutableList.<Message>copyOf(queue);
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
