import java.util.Map;

public class Message {
	private final String message;
	private final Map<Integer,Integer> vector;
	
	public Message(String message, Map<Integer,Integer> vector) {
		this.message = message;
		this.vector = vector;
	}

	public String getMessage() {
		return message;
	}

	public Map<Integer,Integer> getVector() {
		return vector;
	}
}
