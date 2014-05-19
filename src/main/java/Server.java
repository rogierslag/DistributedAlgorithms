import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import lombok.Getter;

@Getter
public class Server extends Thread implements ServerInterface, Serializable {

	private final int me;
	private final int traitors;
	private final int total;

	private final List<Message> queue = new ArrayList<>();
	private int v;
	private int round = 1;
	private boolean decided = false;
	private int traitorType;

	public Server(int me, int total, int traitors) {
		this.me = me;
		this.total = total;
		this.traitors = traitors;
		this.v = randomVal();
		this.traitorType = new Random().nextInt(2);
	}

	private int randomVal() {
		int x = (new Random()).nextInt(2);
		System.out.println("Random val for " + me + " = " + x);
		return x;
	}

	public void broadcast(Message message) {
		if (me < traitors) {
			if ((new Random()).nextInt(1) == this.traitorType) {
				System.err.println("I'm traiting (" + me + ")");
				return;
			}
		}

		for (int i = 0; i < total; i++) {
			try {
				Registry registry = LocateRegistry.getRegistry(Main.PORT);
				ServerInterface stub = (ServerInterface) registry.lookup(Integer.toString(i));
				stub.receive(message);
			}
			catch (RemoteException | NotBoundException e) {
				System.err.println("Client exception: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receive(Message message) throws RemoteException {
		synchronized (queue) {
			if (message != null) {
				queue.add(message);
			}
		}
	}

	@Override
	public void run() {

		while (true) {
			System.out.println("Round number "+round);
			broadcast(new Message(Message.types.NOTIFICATION, v, round, getMe()));
			boolean doneWaiting = false;
			ImmutableList<Message> relevantMessages = ImmutableList.copyOf(new ArrayList<Message>());
			while (!doneWaiting) {
				synchronized (queue) {
					relevantMessages = ImmutableList.copyOf(Collections2.filter(queue, new Predicate<Message>() {
						@Override
						public boolean apply(Message message) {
							return Message.types.NOTIFICATION.equals(message.getType()) && message.getRound() == round;
						}
					}));
				}
				if (relevantMessages.size() >= total - traitors) {
					doneWaiting = true;
				}
				else {
					try {
						Thread.sleep(10);
					}
					catch (InterruptedException e) {
					}
				}

			}

			// Count the occurences
			int[] results = new int[2];
			results[0] = 0;
			results[1] = 0;
			for (Message m : relevantMessages) {
				results[m.getValue()]++;
			}

			int threshold = (total + traitors) / 2;
			if (results[0] > threshold) {
				broadcast(new Message(Message.types.PROPOSAL, 0, round, getMe()));
			}
			else if (results[1] > threshold) {
				broadcast(new Message(Message.types.PROPOSAL, 1, round, getMe()));
			}
			else {
				broadcast(new Message(Message.types.PROPOSAL, -1, round, getMe()));
			}
			if (decided) {
				break;
			}

			doneWaiting = false;
			relevantMessages = ImmutableList.copyOf(new ArrayList<Message>());
			while (!doneWaiting) {
				synchronized (queue) {
					relevantMessages = ImmutableList.copyOf(Collections2.filter(queue, new Predicate<Message>() {
						@Override
						public boolean apply(Message message) {
							return message.getType().equals(Message.types.PROPOSAL) && message.getRound() == round;
						}
					}));
				}
				if (relevantMessages.size() >= total - traitors) {
					doneWaiting = true;
				}
				else {
					try {
						Thread.sleep(10);
					}
					catch (InterruptedException e) {
					}
				}
			}

			results = new int[2];
			results[0] = 0;
			results[1] = 0;
			for (Message m : relevantMessages) {
				if (m.getValue() != -1) {
					results[m.getValue()]++;
				}
			}
			threshold = 3 * traitors;
			if (results[0] > traitors) {
				this.v = 0;
				if (results[0] > threshold) {
					decided = true;
					System.out.println("Decided on: 0");
				}
			}
			else if (results[1] > traitors) {
				this.v = 1;
				if (results[1] > threshold) {
					decided = true;
					System.out.println("Decided on: 1");
				}
			}
			else {
				v = randomVal();
			}
			round++;
			try {
				Thread.sleep((new Random()).nextInt(500));
			}
			catch (InterruptedException e) {
			}
		}
	}
}

