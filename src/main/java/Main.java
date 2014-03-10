import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

	public static final int MAIN_SLEEP = 1000;
	public static final int CANDIDATE_SLEEP = 100;
	public static final int ORDINARY_SLEEP = 100;

	public static final int CANDIDATE_COUNT = 3;
	public static final int ORDINARY_COUNT = 7;

	public static void main(String[] args) {
		List<Candidate> candidates = new ArrayList<>();
		List<Ordinary> ordinaries = new ArrayList<>();
		List<SyncInterface> threads = new ArrayList<>();

		// Create the thingies
		for (int i = 0; i < Main.ORDINARY_COUNT; i++) {
			ordinaries.add(new Ordinary((new Random()).nextInt(1000)));
		}
		for (int i = 0; i < Main.CANDIDATE_COUNT; i++) {
			int id = (new Random()).nextInt(1000)+1000;
			ordinaries.add(new Ordinary(id));
			candidates.add(new Candidate(id,
					ordinaries));
		}

		// Add them to a thread list
		for (int i = 0; i < Main.ORDINARY_COUNT + Main.CANDIDATE_COUNT; i++) {
			threads.add((SyncInterface) ordinaries.get(i));
		}
		for (int i = 0; i < Main.CANDIDATE_COUNT; i++) {
			threads.add((SyncInterface) candidates.get(i));
		}
		
		// Start the threads
		for (int i = 0; i < threads.size(); i++) {
			new Thread(threads.get(i)).start();
		}
		
		System.out.println(threads);

		// Start the threads
		worker: while (true) {
			List<SyncInterface> remove = new ArrayList<>();
			System.out.println("Advancing to the next round");
			for (int i = 0; i < threads.size(); i++) {
				try {
					threads.get(i).nextRound();

				} catch (RemoteException e) {
				}
			}
			for (int i = 0; i < threads.size(); i++) {
				if (threads.get(i).isElected()) {
					System.out.println("Elected someone!" + threads.get(i));
					break worker;
				}
			}
			int j = 0;
			for (int i = 0; i < threads.size(); i++) {
				if (threads.get(i).hasDied()) {
					System.out.println("A hero has fallen");
					remove.add(threads.get(i));
				} else if (threads.get(i) instanceof Candidate) {
					j++;
				}
			}
			if ( j == 0 ) {
				System.out.println("All heroes have died...");
				break worker;
			}
			for (int i = 0; i < remove.size(); i++) {
				threads.remove(remove.get(i));
			}

			
			try {
				Thread.sleep(Main.MAIN_SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}
