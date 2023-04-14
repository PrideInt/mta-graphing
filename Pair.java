/**
 * DO NOT COPY, MODIFY OR DISTRIBUTE
 *
 * - Pride
 */

/**
 * This Pair object takes in two objects of any type (differing, similar, does not matter)
 * and stores it in a single Pair object.
 *
 * @param <L>
 * @param <R>
 */
public class Pair<L, R> {
	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return this.left;
	}
	public L getKey() { return this.left; }

	public R getRight() {
		return this.right;
	}
	public R getValue() { return this.right; }

	public void setLeft(L left) {
		this.left = left;
	}
	public void setRight(R right) {
		this.right = right;
	}

	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<>(left, right);
	}
}
