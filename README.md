This library provides two bus mechanism :

1. `RxBus` : A simple one based on [RxJava](https://github.com/ReactiveX/RxJava)
2. `SortedBus` : A bus with priorities on each listened event and the ability for listeners to consume the event 

# Usage

For both implementation, we first have to define events :

```java
public class MessageEvent { /* Additional fields if needed */ }
```

Then, two steps are needed, according to the implementation used.

## RxBus

1. Register subscriber for a given event

```java
RxBus.getInstance().register(MessageEvent.class)
    .subscribe(event -> ...,
        throwable -> ...);
```

1. Post events

```java
RxBus.getInstance().send(new MessageEvent());
```

## SortedBus

1. Register subscriber for a given event with a priority

```java
SortedBus.getInstance().register(MessageEvent.class, SortedBus.PRIORITY_MEDIUM, new Executor<MessageEvent>() {
    @Override
    public boolean execute(MessageEvent event) {
        return false;   // Return true if you want to stop this event's propagation to other subscribers. False otherwise
    }
});
```

2. Post events

```java
SortedBus.getInstance().send(new MessageEvent());
```

# Installation

Add Beapp's repository in your project's repositories list, then add the dependency.

```groovy
repositories {
    jcenter()
    // ...
    maven { url 'http://repository.beapp.fr/libs-release-local' }
}

dependencies {
    compile 'fr.beapp.bus:bus:1.1'
}
```