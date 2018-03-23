package fr.beapp.bus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * This class provides a bus implementation based on RxJava.
 * <br/>
 * Inspired from: http://nerds.weddingpartyapp.com/tech/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/
 */
public class RxBus {

	private static final RxBus INSTANCE = new RxBus();

	private final Subject<Object> bus = PublishSubject.create();

	private RxBus() {
	}

	public static RxBus getInstance() {
		return INSTANCE;
	}

	/**
	 * Send an event in the bus
	 *
	 * @param value the event to send
	 */
	public void send(Object value) {
		bus.onNext(value);
	}

	/**
	 * @param classListened The event class to listen
	 * @return an Rx {@link Observable} on which received events will be emitted
	 */
	public <T> Observable<T> register(Class<T> classListened) {
		return bus.ofType(classListened);
	}

}