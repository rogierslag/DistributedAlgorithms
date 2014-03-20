import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * The main class handles the main settings of the program.
 * First it initializes the number of candidate nodes and the number of
 * ordinary nodes. It also defines the required sleeps for any node in between
 * rounds.
 */
public class Main {

	public static final int MAIN_SLEEP = 10;
	public static final int CANDIDATE_SLEEP = 0;
	public static final int ORDINARY_SLEEP = 0;

	public static final int CANDIDATE_COUNT = 10;
	public static final int ORDINARY_COUNT = 0;

	public static void main(String[] args) {
		List<Candidate> candidates = new ArrayList<>();
		List<Ordinary> ordinaries = new ArrayList<>();
		List<SyncInterface> threads = new LinkedList<>();

		// Create the thingies
		for (int i = 0; i < Main.ORDINARY_COUNT; i++) {
			ordinaries.add(new Ordinary((new Random()).nextInt(1000),-2));
		}
		for (int i = 0; i < Main.CANDIDATE_COUNT; i++) {
			int id = (new Random()).nextInt(1000)+1000;
            int level = new Random().nextInt(10);
			ordinaries.add(new Ordinary(id,level-2));
			candidates.add(new Candidate(id,
					ordinaries,level));
		}

		// Add them to a thread list
		for (int i = 0; i < Main.ORDINARY_COUNT + Main.CANDIDATE_COUNT; i++) {
			threads.add((SyncInterface) ordinaries.get(i));
		}
		for (int i = 0; i < Main.CANDIDATE_COUNT; i++) {
			threads.add((SyncInterface) candidates.get(i));
		}
		
		System.out.println(threads);
        // For some reason if you place this inside the loop the JVM garbage collection removes some object which are still referenced
        List<SyncInterface> removed = new ArrayList<>();

        // Start the threads
		worker: while (true) {
			System.out.println("Advancing to the next round");

            // This code triggers the seperate threads. Using this we can simulate
            // a synchronious network, which is what the algorith is about
			for (int i = 0; i < threads.size(); i++) {
				try {
					threads.get(i).nextRound();

				} catch (RemoteException e) {
                    e.printStackTrace();
				}
			}

            // We check whether we have found someone who won and then exit the loop
			for (int i = 0; i < threads.size(); i++) {
				if (threads.get(i).isElected()) {
					System.out.println("Elected someone!" + threads.get(i));
					break worker;
				}
			}

            // We check which items have quit and remove them from the thread list
			int j = 0;
			for (int i = 0; i < threads.size(); i++) {
				if (threads.get(i).hasDied()) {
					System.out.println("A hero has fallen");
					removed.add(threads.get(i));
				} else if (threads.get(i) instanceof Candidate) {
					j++;
				}
			}

            // If no more items are present, also quit the loop
			if ( j == 0 ) {
				System.out.println("All heroes have died...");
				break worker;
			}
			for (int i = 0; i < removed.size(); i++) {
				threads.remove(removed.get(i));
			}

            // Do a short nap before proceeding
			try {
				Thread.sleep(Main.MAIN_SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}
