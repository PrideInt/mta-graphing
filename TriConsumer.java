import java.util.Objects;

/**
 * TriConsumer acts as Java's Consumer/BiConsumer except we can have 3 types.
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
	void accept(T t, U u, V v);

	default TriConsumer<T, U, V> andThen(TriConsumer<T, U, V> after) {
		Objects.requireNonNull(after);
		return (t, u, v) -> {
			accept(t, u, v);
			after.accept(t, u, v);
		};
	}
}
