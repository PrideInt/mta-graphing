/**
 * DO NOT COPY, MODIFY OR DISTRIBUTE
 *
 * - Pride
 */

/**
 * This Tuple object takes in three objects of any type (differing, similar, does not matter),
 * similarly to Pair, and stores it in a single Tuple.
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class Tuple<T, U, V> {
	private T first;
	private U second;
	private V third;

	public Tuple(T first, U second, V third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() {
		return this.first;
	}

	public U getSecond() {
		return this.second;
	}

	public V getThird() {
		return this.third;
	}

	public static <T, U, V> Tuple<T, U, V> of(T first, U second, V third) {
		return new Tuple<>(first, second, third);
	}
}
