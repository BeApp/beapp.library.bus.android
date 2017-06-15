package fr.beapp.bus;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;

public class RxBusTest {

    private final RxBus rxBus = RxBus.getInstance();
    private TestObserver<Integer> testObserver1;
    private TestObserver<Integer> testObserver2;

    @Before
    public void initTest() {
        testObserver1 = TestObserver.create();
        testObserver2 = TestObserver.create();
    }

    @Test
    public void testRegister_subscribed_noValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        rxBus.send("otherEvent");

        testObserver1.assertNoValues();
        testObserver2.assertNoValues();
    }

    @Test
    public void testRegister_subscribed_oneValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        rxBus.send("otherEvent");
        rxBus.send(1);

        testObserver1.assertValueCount(1);
        testObserver1.assertValue(1);
        testObserver2.assertValueCount(1);
        testObserver2.assertValue(1);
    }

    @Test
    public void testRegister_subscribed_threeValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        rxBus.send(1);
        rxBus.send("otherEvent");
        rxBus.send(2);
        rxBus.send(3);

        testObserver1.assertValueCount(3);
        testObserver1.assertValues(1, 2, 3);
        testObserver2.assertValueCount(3);
        testObserver2.assertValues(1, 2, 3);
    }

    @Test
    public void testRegister_mixinSubscribed() {
        TestObserver<String> testObserverString = TestObserver.create();

        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(String.class).subscribe(testObserverString);
        rxBus.send(1);
        rxBus.send("event1");
        rxBus.send(2);
        rxBus.send("event2");

        testObserver1.assertValueCount(2);
        testObserver1.assertValues(1, 2);
        testObserverString.assertValueCount(2);
        testObserverString.assertValues("event1", "event2");
    }

    @Test
    public void testRegister_unsubscribed_noValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        testObserver2.dispose();
        rxBus.send("otherEvent");

        testObserver1.assertNoValues();
        testObserver2.assertNoValues();
    }

    @Test
    public void testRegister_unsubscribed_oneValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        testObserver2.dispose();
        rxBus.send("otherEvent");
        rxBus.send(1);

        testObserver1.assertValueCount(1);
        testObserver1.assertValue(1);
        testObserver2.assertNoValues();
    }

    @Test
    public void testRegister_unsubscribed_threeValue() {
        rxBus.register(Integer.class).subscribe(testObserver1);
        rxBus.register(Integer.class).subscribe(testObserver2);
        testObserver2.dispose();
        rxBus.send("otherEvent");
        rxBus.send(1);
        rxBus.send(2);
        rxBus.send(3);

        testObserver1.assertValueCount(3);
        testObserver1.assertValues(1, 2, 3);
        testObserver2.assertNoValues();
    }

    @Test
    public void testSameReference() {
        TestObserver<DummyEvent> testReferenceObservable = TestObserver.create();

        rxBus.register(DummyEvent.class)
                .doOnNext(new Consumer<DummyEvent>() {
                    @Override
                    public void accept(DummyEvent dummyEvent) throws Exception {
                        dummyEvent.setValue(2);
                    }
                })
                .subscribe(testReferenceObservable);
        rxBus.send(new DummyEvent(1));

        testReferenceObservable.assertValueCount(1);
        testReferenceObservable.assertValueAt(0, new Predicate<DummyEvent>() {
            @Override
            public boolean test(DummyEvent dummyEvent) throws Exception {
                return dummyEvent.getValue() == 2;
            }
        });
    }

}
