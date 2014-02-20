import java.util.ArrayList;

public class Message {
	private final String message;
	private final ArrayList<Integer> vector;
	
	public Message(String message, ArrayList<Integer> vector) {
		this.message = message;
		this.vector = vector;
	}

	public String getMessage() {
		return message;
	}

	public ArrayList<Integer> getVector() {
		return vector;
	}
}
