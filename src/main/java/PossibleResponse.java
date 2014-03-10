import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class PossibleResponse implements Comparable<PossibleResponse> {
	int level;
	int sender;
	Candidate c;
	
	@Override
	public int compareTo(PossibleResponse o) {
		if ( o == null ) {
			return -1;
		}
		int lvlDiff = level - o.getLevel();
		if ( lvlDiff < 0 ) {
			return -1;
		} else if ( lvlDiff > 0 ) {
			return 1;
		}
		int send = sender - o.getSender();
		if ( send < 0 ) {
			return -1;
		} else if ( send > 0 ) {
			return 1;
		} else {
			return 0;
		}
	}
}
