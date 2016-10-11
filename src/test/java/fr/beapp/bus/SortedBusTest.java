package fr.beapp.bus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortedBusTest {

	private final SortedBus sortedBus = SortedBus.getInstance();
	private AssertExecutor<Integer> executor1;
	private AssertExecutor<Integer> executor2;

	@Before
	public void setup() {
		executor1 = new AssertExecutor<>();
		executor2 = new AssertExecutor<>();
	}

	@After
	public void teardown() {
		sortedBus.unregisterAll();
	}

	@Test
	public void testRegister_subscribed_noValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2);
		sortedBus.send("otherEvent");

		executor1.assertNoValues();
		executor2.assertNoValues();
	}

	@Test
	public void testRegister_subscribedSamePriority() {
		AssertExecutor<Integer> executor3 = new AssertExecutor<>();

		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor3);
		sortedBus.send("otherEvent");
		sortedBus.send(1);

		executor1.assertValueCount(1);
		executor1.assertValue(1);
		executor2.assertNoValues();
		executor3.assertNoValues();
	}

	@Test
	public void testRegister_subscribed_oneValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2);
		sortedBus.send("otherEvent");
		sortedBus.send(1);

		executor1.assertValueCount(1);
		executor1.assertValue(1);
		executor2.assertValueCount(1);
		executor2.assertValue(1);
	}

	@Test
	public void testRegister_subscribed_oneValue_consumedHigh() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2.willConsumeEvent());
		sortedBus.send("otherEvent");
		sortedBus.send(1);

		executor1.assertNoValues();
		executor2.assertValueCount(1);
		executor2.assertValue(1);
	}

	@Test
	public void testRegister_subscribed_threeValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2);
		sortedBus.send(1);
		sortedBus.send("otherEvent");
		sortedBus.send(2);
		sortedBus.send(3);

		executor1.assertValueCount(3);
		executor1.assertValues(1, 2, 3);
		executor2.assertValueCount(3);
		executor2.assertValues(1, 2, 3);
	}

	@Test
	public void testRegister_subscribed_threeValue_consumedHigh() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2.willConsumeEvent());
		sortedBus.send(1);
		sortedBus.send("otherEvent");
		sortedBus.send(2);
		sortedBus.send(3);

		executor1.assertNoValues();
		executor2.assertValueCount(3);
		executor2.assertValues(1, 2, 3);
	}

	@Test
	public void testRegister_mixinSubscribed() {
		AssertExecutor<String> executorString = new AssertExecutor<>();

		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.register(String.class, SortedBus.PRIORITY_MEDIUM, executorString);
		sortedBus.send(1);
		sortedBus.send("event1");
		sortedBus.send(2);
		sortedBus.send("event2");

		executor1.assertValueCount(2);
		executor1.assertValues(1, 2);
		executorString.assertValueCount(2);
		executorString.assertValues("event1", "event2");
	}

	@Test
	public void testRegister_mixinSubscribed_consumed() {
		AssertExecutor<String> executorString = new AssertExecutor<>();

		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1.willConsumeEvent());
		sortedBus.register(String.class, SortedBus.PRIORITY_MEDIUM, executorString);
		sortedBus.send(1);
		sortedBus.send("event1");
		sortedBus.send(2);
		sortedBus.send("event2");

		executor1.assertValueCount(2);
		executor1.assertValues(1, 2);
		executorString.assertValueCount(2);
		executorString.assertValues("event1", "event2");
	}

	@Test
	public void testRegister_unsubscribed_noValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.unregister(Integer.class, sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2));
		sortedBus.send("otherEvent");

		executor1.assertNoValues();
		executor2.assertNoValues();
	}

	@Test
	public void testRegister_unsubscribed_oneValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.unregister(Integer.class, sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2));
		sortedBus.send("otherEvent");
		sortedBus.send(1);

		executor1.assertValueCount(1);
		executor1.assertValue(1);
		executor2.assertNoValues();
	}

	@Test
	public void testRegister_unsubscribed_threeValue() {
		sortedBus.register(Integer.class, SortedBus.PRIORITY_MEDIUM, executor1);
		sortedBus.unregister(Integer.class, sortedBus.register(Integer.class, SortedBus.PRIORITY_HIGH, executor2));
		sortedBus.send("otherEvent");
		sortedBus.send(1);
		sortedBus.send(2);
		sortedBus.send(3);

		executor1.assertValueCount(3);
		executor1.assertValues(1, 2, 3);
		executor2.assertNoValues();
	}

	private static class AssertExecutor<T> implements SortedBus.Executor<T> {
		private boolean consumeEvent = false;
		private List<T> values = new ArrayList<>();

		public AssertExecutor<T> willConsumeEvent() {
			this.consumeEvent = true;
			return this;
		}

		@Override
		public boolean execute(T event) {
			values.add(event);
			return consumeEvent;
		}

		public void assertNoValues() {
			Assert.assertEquals("No received events expected yet some received: " + values.size(), 0, values.size());
		}

		public void assertValueCount(int count) {
			Assert.assertEquals("Number of received values differ; expected: " + count + ", actual: " + values.size(), count, values.size());
		}

		public void assertValue(T value) {
			assertItem(value, 0);
		}

		@SafeVarargs
		public final void assertValues(T... values) {
			Assert.assertEquals("Number of items does not match. Expected: " + values.length + "  Actual: " + this.values.size()
					+ ".\n"
					+ "Expected values: " + Arrays.toString(values)
					+ "\n"
					+ "Actual values: " + this.values
					+ "\n", values.length, this.values.size());


			for (int i = 0; i < values.length; i++) {
				assertItem(values[i], i);
			}
		}

		private void assertItem(T expected, int index) {
			T actual = values.get(index);
			if (expected == null) {
				Assert.assertNull("Value at index: " + index + " expected to be [null] but was: [" + actual + "]\n", actual);
			}

			Assert.assertEquals("Value at index: " + index
					+ " expected to be [" + expected + "] (" + expected.getClass().getSimpleName()
					+ ") but was: [" + actual + "] (" + (actual != null ? actual.getClass().getSimpleName() : "null") + ")\n", expected, actual);
		}

	}

}