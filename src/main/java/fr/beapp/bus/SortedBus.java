package fr.beapp.bus;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class provides a bus mechanism with priority and event consumption.
 * When registering to a given event class to listen, a priority must be chose too in order to order the registration.
 * <p>When an event is sent, we retrieve executors listening for the event's class and call them. If one of the executors mark the event as consumed (ie: return <code>true</code>) we stop the propagation to this executor</p>
 * <p>Be careful : Multiple executors can't be registered with the same priority level for a same class.</p>
 */
public class SortedBus {

	public static final int PRIORITY_HIGH = 100;
	public static final int PRIORITY_MEDIUM = 500;
	public static final int PRIORITY_LOW = 1000;

	private static final SortedBus INSTANCE = new SortedBus();
	private static final Map<Class, Set<PriorityExecutor>> ALL_EXECUTORS = new ConcurrentHashMap<>();

	private SortedBus() {
	}

	public static SortedBus getInstance() {
		return INSTANCE;
	}

	/**
	 * Send an event in the bus
	 *
	 * @param value the event to send
	 * @return the number of executors who received the event
	 */
	public synchronized int send(Object value) {
		return send(value, value.getClass());
	}

	/**
	 * Send an event in the bus by specifying the class to use to retrieve executors
	 *
	 * @param value   the event to send
	 * @param asClazz the class to use to retrieve executors
	 * @return the number of executors who received the event
	 */
	@SuppressWarnings("unchecked")
	public synchronized int send(Object value, Class<?> asClazz) {
		int receiversCount = 0;

		Set<PriorityExecutor> executors = ALL_EXECUTORS.get(asClazz);
		if (executors != null) {
			for (PriorityExecutor priorityExecutor : executors) {
				receiversCount++;
				if (priorityExecutor.executor.execute(value)) {
					break;
				}
			}
		}
		return receiversCount;
	}

	/**
	 * Register a new executor to listen on the given class with a specific priority
	 *
	 * @param classListened The event class to listen
	 * @param <T> The type parameter
	 * @param priority      The priority to use
	 * @param executor      The executor to execute when the event is received
	 * @return The executor's identifier
	 */
	public synchronized <T> int register(Class<T> classListened, int priority, Executor<T> executor) {
		Set<PriorityExecutor> executors = ALL_EXECUTORS.get(classListened);
		if (executors == null) {
			executors = new TreeSet<>();
			ALL_EXECUTORS.put(classListened, executors);
		}

		PriorityExecutor priorityExecutor = new PriorityExecutor(priority, executor);
		executors.add(priorityExecutor);
		return priorityExecutor.id;
	}

	/**
	 * Unregister an executor for a given class by using his registration's identifier
	 *
	 * @param classListened The class listened by the executor
	 * @param <T> The type parameter
	 * @param id            The registration identifier
	 * @return <code>true</code> if the executor was found and removed, <code>false</code> otherwise
	 */
	public synchronized <T> boolean unregister(Class<T> classListened, int id) {
		Set<PriorityExecutor> executors = ALL_EXECUTORS.get(classListened);
		if (executors != null) {
			for (PriorityExecutor priorityExecutor : executors) {
				if (priorityExecutor.id == id) {
					executors.remove(priorityExecutor);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Unregister all executors for a given class
	 *
	 * @param classListened The class listened by the executor
	 * @param <T> The type parameter
	 * @return <code>true</code> if the executors were found and removed, <code>false</code> otherwise
	 */
	public synchronized <T> boolean unregisterAll(Class<T> classListened) {
		return ALL_EXECUTORS.remove(classListened) != null;
	}

	/**
	 * Unregister all executors
	 */
	public synchronized void unregisterAll() {
		ALL_EXECUTORS.clear();
	}

	/**
	 * An executor interface to implement in order to handle received events.
	 */
	public interface Executor<T> {
		/**
		 * @param event The received event
		 * @return <code>true</code> if this executor consumes the event (and prevent his propagation on lower priority levels), <code>false</code> otherwise
		 */
		boolean execute(T event);
	}

	private static class PriorityExecutor implements Comparable<PriorityExecutor> {
		private static final AtomicInteger nextExecutorId = new AtomicInteger(1);

		private final int id;
		private final int priority;
		private final Executor executor;

		public PriorityExecutor(int priority, Executor executor) {
			this.id = nextExecutorId.getAndIncrement();
			this.priority = priority;
			this.executor = executor;
		}

		@Override
		public int compareTo(PriorityExecutor another) {
			return (priority < another.priority) ? -1 : ((priority == another.priority) ? 0 : 1);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			PriorityExecutor that = (PriorityExecutor) o;

			return id == that.id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}

}
