import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class PossibleResponse {
    /**
     * The level this PossibleResponse was triggered in
     */
	int level;

    /**
     * The ID of the sender
     */
	int sender;

    /**
     * The candidate associated with this
     */
	Candidate candidate;
	
    public String toString() {
        return String.format("<level %d, sender %d>",level,sender);
    }
}
