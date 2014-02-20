import java.util.ArrayList;
import java.util.Map;

public class Message {
	private final String message;
	private final int sender;
	private final Map<Integer,Integer> vector;
	
	public Message(String message, Map<Integer,Integer> vector, int sender ) {
		this.message = message;
		this.vector = vector;
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public Map<Integer,Integer> getVector() {
		return vector;
	}
	
	public int getSender() {
		return sender;
	}
}
