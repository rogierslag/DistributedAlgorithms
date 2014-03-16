import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class PossibleResponse implements Comparable<PossibleResponse> {
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
	
	@Override
    /**
     * Provide a compareTo method for sorting
     */
	public int compareTo(PossibleResponse o) {
		if ( o == null ) {
			return -1;
		}
		int send = (sender/Math.abs(sender)) - ( o.getSender()/Math.abs(o.getSender()));
		if ( send < 0 ) {
			return -1;
		} else if ( send > 0 ) {
			return 1;
		}
        int lvlDiff = level - o.getLevel();
        if ( lvlDiff < 0 ) {
            return -1;
        } else if ( lvlDiff > 0 ) {
            return 1;
        } else {
			return 0;
		}
	}

    public String toString() {
        return String.format("<level %d, sender %d>",level,sender);
    }
}
