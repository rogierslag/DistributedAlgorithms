import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Rogier on 19-05-14.
 */
@AllArgsConstructor
@Data
public class Message implements Serializable {

	private final Message.types type;
	private final int value;
	private final int round;
	private final int sendingServer;

	public enum types {
		NOTIFICATION,
		PROPOSAL
	}
}
