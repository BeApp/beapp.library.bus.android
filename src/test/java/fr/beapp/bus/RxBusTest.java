package fr.beapp.bus;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;

public class RxBusTest {

	private final RxBus rxBus = RxBus.getInstance();
	private TestSubscriber<Integer> testSubscriber1;
	private TestSubscriber<Integer> testSubscriber2;

	@Before
	public void initTest() {
		testSubscriber1 = TestSubscriber.create();
		testSubscriber2 = TestSubscriber.create();
	}

	@Test
	public void testRegister_subscribed_noValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2);
		rxBus.send("otherEvent");

		testSubscriber1.assertNoValues();
		testSubscriber2.assertNoValues();
	}

	@Test
	public void testRegister_subscribed_oneValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2);
		rxBus.send("otherEvent");
		rxBus.send(1);

		testSubscriber1.assertValueCount(1);
		testSubscriber1.assertValue(1);
		testSubscriber2.assertValueCount(1);
		testSubscriber2.assertValue(1);
	}

	@Test
	public void testRegister_subscribed_threeValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2);
		rxBus.send(1);
		rxBus.send("otherEvent");
		rxBus.send(2);
		rxBus.send(3);

		testSubscriber1.assertValueCount(3);
		testSubscriber1.assertValues(1, 2, 3);
		testSubscriber2.assertValueCount(3);
		testSubscriber2.assertValues(1, 2, 3);
	}

	@Test
	public void testRegister_mixinSubscribed() {
		TestSubscriber<String> testSubscriberString = TestSubscriber.create();

		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(String.class).subscribe(testSubscriberString);
		rxBus.send(1);
		rxBus.send("event1");
		rxBus.send(2);
		rxBus.send("event2");

		testSubscriber1.assertValueCount(2);
		testSubscriber1.assertValues(1, 2);
		testSubscriberString.assertValueCount(2);
		testSubscriberString.assertValues("event1", "event2");
	}

	@Test
	public void testRegister_unsubscribed_noValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2).unsubscribe();
		rxBus.send("otherEvent");

		testSubscriber1.assertNoValues();
		testSubscriber2.assertNoValues();
	}

	@Test
	public void testRegister_unsubscribed_oneValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2).unsubscribe();
		rxBus.send("otherEvent");
		rxBus.send(1);

		testSubscriber1.assertValueCount(1);
		testSubscriber1.assertValue(1);
		testSubscriber2.assertNoValues();
	}

	@Test
	public void testRegister_unsubscribed_threeValue() {
		rxBus.listen(Integer.class).subscribe(testSubscriber1);
		rxBus.listen(Integer.class).subscribe(testSubscriber2).unsubscribe();
		rxBus.send("otherEvent");
		rxBus.send(1);
		rxBus.send(2);
		rxBus.send(3);

		testSubscriber1.assertValueCount(3);
		testSubscriber1.assertValues(1, 2, 3);
		testSubscriber2.assertNoValues();
	}

	@Test
	public void testSameReference() {
		TestSubscriber<DummyEvent> testReferenceSubscriber = TestSubscriber.create();

		rxBus.listen(DummyEvent.class)
				.doOnNext(dummyEvent -> dummyEvent.setValue(2))
				.subscribe(testReferenceSubscriber);
		rxBus.send(new DummyEvent(1));

		testReferenceSubscriber.assertValueCount(1);
		Assert.assertEquals(2, testReferenceSubscriber.getOnNextEvents().get(0).getValue());
	}

}
