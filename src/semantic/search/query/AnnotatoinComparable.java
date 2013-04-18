package semantic.search.query;

import java.util.Comparator;

public class AnnotatoinComparable<T> implements Comparator<T> {
	public int compare(Annotation anno1, Annotation anno2) {
		if(anno1.getScore() > anno2.getScore())
			return 1;
		else
			return 0;
	}

	@Override
	public int compare(T anno1, T anno2) {
		if(((Annotation) anno1).getScore() > ((Annotation)anno2).getScore())
			return 1;
		else
			return 0;
	}

}
