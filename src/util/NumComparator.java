package util;

import java.util.Comparator;

public class NumComparator<T extends Comparable<T>> implements Comparator<T> {
	@Override
	public boolean equals(Object arg0) {
		return this.equals(arg0);
	}
	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
