import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Synchronized;

@AllArgsConstructor
public class Message implements Serializable {
	private static final long serialVersionUID = 8689683240497397154L;
	
	private final String message;
	private final Map<Integer,Integer> vector;
	private final int sender;

	@Synchronized
	public String getMessage() {
		return message;
	}

	@Synchronized
	public Map<Integer,Integer> getVector() {
		return vector;
	}
	
	@Synchronized
	public int getSender() {
		return sender;
	}
}
