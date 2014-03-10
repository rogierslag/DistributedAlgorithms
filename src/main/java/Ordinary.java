import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.sound.midi.SysexMessage;

public class Ordinary implements OrdinaryInterface {

	private List<PossibleResponse> responses = new ArrayList<PossibleResponse>();
	private int level = Integer.MIN_VALUE;
	private int id;
	private Candidate sendTo = null;

	public Ordinary() {
		this.id = new Random().nextInt();
		System.out.println(new StringBuilder("I was just initialized as ")
				.append(this).append(" with an id of ").append(this.id)
				.toString());
	}

	public Ordinary(int id) {
		this.id = id;
		System.out.println(new StringBuilder("I was just initialized as ")
				.append(this).append(" with an id of ").append(this.id)
				.toString());
	}

	@Override
	public void check(PossibleResponse candidate) throws RemoteException {
		responses.add(candidate);
	}

	public void run() {
		level++;
		Collections.sort(responses);
		if (responses.size() == 0) {
			sendTo = null;
			return;
		}
		PossibleResponse R = responses.get(responses.size() - 1);
		if (R.compareTo(new PossibleResponse(level, id, null)) > 0) {
			level = R.level;
			id = R.sender;
			sendTo = R.c;
		} else {
			sendTo = null;
		}
		responses = new ArrayList<>();
		// send
		if (sendTo != null) {
			try {
				sendTo.ack(this);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void nextRound() throws RemoteException {
		try {
			Thread.sleep(Main.ORDINARY_SLEEP);
			this.run();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isElected() {
		return false;
	}

	@Override
	public boolean hasDied() {
		return false;
	}

}
