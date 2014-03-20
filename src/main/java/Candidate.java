import lombok.Synchronized;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.SysexMessage;

/**
 * The candidate process may win the election
 */
public class Candidate implements CandidateInterface {

    /**
     * the level of the algorithm
     */
	private int level = -1;

    /**
     * A list of all the ordinaries to send a message to
     */
	private final List<Ordinary> links;

    /**
     * A list of all the ordinaries we are waiting for a response
     */
	private List<Ordinary> sent;

    /**
     * The ID of the candidate
     */
	private int id;

    /**
     * Whether the candidate died already
     */
	private boolean died = false;

    /**
     * Whether the candidate has been elected
     */
	private boolean elected = false;

    /**
     * Initialize a candidate with an id and give it a list of links and log some data
     * @param id the id
     * @param links a list of all the ordinaries
     */
	public Candidate(int id, List<Ordinary> links) {
		this.links = new ArrayList<>(links);
		this.sent = new ArrayList<>();
		this.id = id;
		System.out.println(new StringBuilder("I was just initialized as ")
				.append(this).append(" with an id of ").append(id).toString());
	}

    @Synchronized
    /**\
     * Determine which step of the algorithm to run
     */
    public void run() {
		level++;
		if (isEven()) {
			sendAction();
		} else {
			receiveAction();
		}
	}

    /**
     * Send a request to some ordinaries
     */
	public void sendAction() {
        if ( died ) {
            return;
        }
        // If there are no more links here I've won!
		if (links.isEmpty()) {
			died = true;
			elected = true;
			System.out.println("I'm elected! id:" + id);
		} else {
            // Else I still need confirmation of more links
			System.out.println("Still need " + links);
            // Randomize the links
			//Collections.shuffle(links);
            // Get to how many ordinaries I should send a message
			int k = Math.min((int) Math.pow(2, (level / 2)), links.size());
			sent = new ArrayList<Ordinary>();
            PossibleResponse R = new PossibleResponse(level, id, this);
            Collections.shuffle(links);
			for (int i = 0; i < k; i++) {
                // Send a message to the k ordinaries
				try {
					links.get(i).check(R);
					sent.add(links.get(i));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
            // And remove those links from the todo list
			for (int i = 0; i < sent.size(); i++) {
				links.remove(sent.get(i));
			}
		}
	}

    /**
     * I should check how many responses I've gotten. If the list is not empty I'm not the highest
     */
	public void receiveAction() {
        if ( died) {
            return;
        }
		if (!sent.isEmpty()) {
			System.err.println(new StringBuilder(this.toString())
					.append(" still waits for ").append(sent).toString());
			died = true;
		}
	}

    /**
     * Short helper to determine even numbers
     * @return true if the number is even, false otherwise
     */
	public boolean isEven() {
		return level % 2 == 0;
	}

	@Override
    /**
     * If I receive an ack from an ordinary I should remove that ordinary from the waiting-for list
     */
	public void ack(Ordinary ordinary) throws RemoteException {
		if (!sent.remove(ordinary)) {
            System.out.println("I tried to remove an nonexisting element..."+ ordinary + " but I have " + sent);
        }
	}

	@Override
    /**
     * This method emulates a synchronious network
     */
    public void nextRound() throws RemoteException {
		try {
			Thread.sleep(Main.CANDIDATE_SLEEP);
			this.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
    /**
     * Whether I'm elected
     */
	public boolean isElected() {
		return elected;
	}

	@Override
    /**
     * Whether I've died yet.
     */
	public boolean hasDied() {
		return died;
	}

    public String toString() { return id+""; }
}
