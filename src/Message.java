import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {
	private static final long serialVersionUID = 8689683240497397154L;
	
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
