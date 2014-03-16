import lombok.Synchronized;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This is the ordinary interface. Although it participates in the algorithm it can never win
 */
public class Ordinary implements OrdinaryInterface {

    /**
     * This is a list of the items it received. It will send a response to only one
     */
    private List<PossibleResponse> responses = new ArrayList<>();

    /**
     * The current level in which the algorithm is.
     */
    private int level = -2;

    /**
     * The id of the ordinary process
     */
    private int id;

    /**
     * To which a reply should be send
     */
    private Candidate sendTo = null;

    /**
     * Start the ordinary and log some data
     */
    public Ordinary() {
        this.id = new Random().nextInt();
        System.out.println(new StringBuilder("I was just initialized as ")
                .append(this).append(" with an id of ").append(this.id)
                .toString());
    }

    /**
     * Start the ordinary with a predefined id and log some data
     * @param id the ID to initialize with
     */
    public Ordinary(int id) {
        this.id = id;
        System.out.println(new StringBuilder("I was just initialized as ")
                .append(this).append(" with an id of ").append(this.id)
                .toString());
    }

    @Override
    /**
     * This is the interface call through RMI. The candidate send a message to this
     * method and may receive an answer in the next iteration
     */
    public void check(PossibleResponse candidate) throws RemoteException {
        if (candidate != null) {
            //System.out.println(candidate);
            responses.add(candidate);
        }
    }

    @Synchronized
    /**
     * The algorithm itself for ordinary processes
     */
    public void run() {
        // Increase the level
        level++;
        sendTo = null;
        // Sort the responses (using PossibleResponse.compareTo) to order the responses
        responses.remove(null);
        Collections.sort(responses);
        // When no responses are present, terminate this loop
        if (responses.size() == 0) {
            return;
        }
        // Get the highest ranking response of the list
        responses.remove(null);
        PossibleResponse R = responses.get(responses.size()-1);
        // Compare this to the values of the ordinary
        if (R.getSender() >= id || R.getLevel() >= level) {
            // If the response is larger, send a response
            level = R.level;
            id = R.sender;
            sendTo = R.candidate;
        } else {
            // Else don't
            sendTo = null;
        }
        // Reset the response list
        responses = new ArrayList<>();
        // send the response if necessary
        if (sendTo != null) {
            try {
                sendTo.ack(this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    /**
     * This method emulates a synchronious network
     */
    public void nextRound() throws RemoteException {
        try {
            Thread.sleep(Main.ORDINARY_SLEEP);
            this.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    /**
     * An ordinary is never elected
     */
    public boolean isElected() {
        return false;
    }

    @Override
    /**
     * And it never dies
     */
    public boolean hasDied() {
        return false;
    }

}
