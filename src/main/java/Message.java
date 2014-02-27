import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Message implements Serializable {
	private static final long serialVersionUID = 8689683240497397154L;
	
	private final String message;
	private final Map<Integer,Integer> vector;
	private final int sender;

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
