瓦解核心：视界 | Brea:Horiz
=======

![clash](/horiz_long.png)

[点击查看中文版本](README.md)

---

# Overview

Horiz Lib provides an easy-to-use event forwarding feature that allows events related to entities in the game to be
forwarded to the event handlers held by the entities, avoiding cumbersome event registration and dynamic addition and
removal.

The logic is: use `AttachmentType` to store a list of handlers for a specific event; listen for events related to the
entity, get the `data attachment` from the event, and execute the corresponding handlers contained in the event, for
example:

```java
public static final Consumer<EntityEvent> consumer = event -> event.getEntity().getExistingData(Horiz.EVENT_DISTRIBUTE).ifPresent(d -> d.post(event));

@SubscribeEvent
public static void hurtIncome(LivingIncomingDamageEvent event) {
    consumer.accept(event);
}
```

This process makes the variable event handling behavior for a single entity more flexible and convenient. You can use
this process to implement features such as "Tinkers' Construct equipment traits," "accessory effects," and "entity skill
systems."

# How to use this system?

First, we need to get the corresponding server-side `Entity`. After obtaining the target entity, you can use:

```java
public <T extends Event> void initEntity() {
    Entity entity; // the entity you obtained
    Class<T> eventClazz; // the class of the event to listen to
    Consumer<T> eventConsumer; // the handler for the event

    // IdentEvent contains the class of the event and its corresponding handler, used to manage added listeners
    IdentEvent<T> ident = new IdentEvent(eventClazz, eventConsumer);

    EntityEventDistribute distribute = entity.getData(Horiz.EVENT_DISTRIBUTE);

    distribute.add(ident); // use the add method to add a new listener
    distribute.remove(ident); // use the remove method to clear an existing listener
    distribute.removeByClass(eventClazz); // or use removeByClass to clear all listeners for a specific event

    // Note: The scope is the listeners within the entity distributor, which does not affect the registration of the game event bus and other entity distributors
}
```

Additionally, you can quickly remove events by adding tags to them when needed.

Imagine a folder—you can add files to it, or you can add other folders. The design of tags is similar. In the `add`
method of [`EntityEventDistribute`](src/main/java/com/phasetranscrystal/horiz/EntityEventDistribute.java), it allows
adding a set of `ResourceLocation` as the tag path of the listener after `IdentEvent`, and in the `removeMarked` method,
you can quickly remove listeners based on this path.

Let's demonstrate with an example:

```java
static {
    ResourceLocation loc1, loc2, loc3, loc4;
    IdentEvent<?> i1, i2, i3, i4, i5, i6, i7, i8, i9;
    EntityEventDistribute distribute;

    distribute.add(i1);
    distribute.add(i2);
    distribute.add(i3, loc1);
    distribute.add(i4, loc1, loc2);
    distribute.add(i5, loc1, loc2);
    distribute.add(i6, loc1, loc3);
    distribute.add(i7, loc1, loc3, loc4);
    distribute.add(i8, loc2);
    distribute.add(i9, loc2, loc3);
    // The stored structure is roughly like this:
    // root
    // |-(i1),(i2)
    // |-loc1
    // | |-(i3)
    // | |-loc2
    // | |  \(i4),(i5)
    // | \loc3
    // |   |-(i6)
    // |   \loc4
    // |     \(i7)
    // \loc2
    //   |-(i8)
    //   \loc3
    //     \(i9)

    // Assuming the following method is executed and distribute is automatically restored (it actually won't)
    distribute.removeMarked(loc1, loc3, loc4); // remove i7
    distribute.removeMarked(loc1, loc3); // remove i6, i7
    distribute.removeMarked(loc1, loc2); // remove i4, i5
    distribute.removeMarked(loc1); // remove i3, i4, i5, i6, i7
    distribute.removeMarkedSelf(loc1); // remove i3, do not remove contents under sub-paths
    distribute.removeMarked(loc2); // remove i8, i9
}
```

# Adding more event forwarding

As we mentioned at the beginning, the logic for forwarding events is completed in the `EventConsumer#consumer` constant.
You only need to listen to existing events and use the `Consumer#accept` method to execute them. This process simplifies
the process of obtaining entity data attachments. If your event is not an `EntityEvent`, you may need to complete the
corresponding logic yourself.

# Other Q&A

## When should I initialize the event listeners for forwarding?

You can initialize directly when the entity is created. If you cannot do this or need to globally initialize various
entities, you can listen to
the [`GatherEntityDistributeEvent`](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java) event, which is
forwarded from the lowest priority of the entity joining the world.

Note: Since this data attachment does not have a configured `Codec`, the events in the forwarding will not be saved—this
means that when the entity is reloaded or teleported to another dimension, you need to reinitialize the events.
Therefore, we recommend using `GatherEntityDistributeEvent`.

## I want to handle situations where the entity causes damage to the target/entity kills the target, what should I do?

The library provides forwarding for damage events and kill events—you can find the corresponding events forwarded at
the [bottom of the `EventConsumer` class](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java).

## Which events does the mod already provide forwarding for?

You can see the forwarding provided by the mod
under [`EventConsumer#bootstrapConsumer()`](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java).

## If the same event is forwarded by different mods, will the listener be executed multiple times?

No. In the [`EntityEventDistribute`](src/main/java/com/phasetranscrystal/horiz/EntityEventDistribute.java) class, there
is a table that records the hash values of events. If the same instance of the same event is forwarded multiple times,
the forwarding after the first time will be filtered out.

# Contribution

Contributions to the Horiz mod are welcome. You can submit issues to report problems or fork this project to submit
your improvements.

# Developers

- **Mon-landis**: Provides technical support and development.


