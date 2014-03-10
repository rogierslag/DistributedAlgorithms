import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.SysexMessage;

public class Candidate implements CandidateInterface {

	private int level = -1;
	private final List<Ordinary> links;
	private List<Ordinary> sent;
	private int id;
	private boolean died = false;
	private boolean elected = false;

	public Candidate(int id, List<Ordinary> links) {
		this.links = links;
		this.sent = new ArrayList<>();
		this.id = id;
		System.out.println(new StringBuilder("I was just initialized as ")
				.append(this).append(" with an id of ").append(id).toString());
	}

	public void run() {
		level++;
		if (isEven()) {
			sendAction();
		} else {
			receiveAction();
		}
	}

	public void sendAction() {
		if (links.isEmpty()) {
			died = true;
			elected = true;
			System.out.println("I'm elected! id:" + id);
		} else {
			System.out.println("Still need " + links);
			Collections.shuffle(links);
			int k = Math.min((int) Math.pow(2, (level / 2)), links.size());
			sent = new ArrayList<Ordinary>();
			for (int i = 0; i < k; i++) {
				try {
					links.get(i).check(new PossibleResponse(level, id, this));
					sent.add(links.get(i));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < sent.size(); i++) {
				links.remove(sent.get(i));
			}
		}
	}

	public void receiveAction() {
		if (!sent.isEmpty()) {
			System.err.println(new StringBuilder(this.toString())
					.append(" still waits for ").append(sent).toString());
			died = true;
		}
	}

	public boolean isEven() {
		return level % 2 == 0;
	}

	@Override
	public void ack(Ordinary ordinary) throws RemoteException {
		sent.remove(ordinary);
	}

	@Override
	public void nextRound() throws RemoteException {
		try {
			Thread.sleep(Main.CANDIDATE_SLEEP);
			this.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isElected() {
		return elected;
	}

	@Override
	public boolean hasDied() {
		return died;
	}
}
