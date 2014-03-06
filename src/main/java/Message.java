import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Message implements Serializable {
	private static final long serialVersionUID = 8689683240497397154L;
	
	private final String message;
	private final List<Integer> vector;
	private final int sender;

	public String getMessage() {
		return message;
	}

	public List<Integer> getVector() {
		return vector;
	}
	
	public int getSender() {
		return sender;
	}
}
