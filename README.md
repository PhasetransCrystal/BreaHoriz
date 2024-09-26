瓦解核心：视界 | Brea:Horiz
=======

![clash](/horiz_long.png)

[click here to read English version](README_en.md)

---

# 概述

Horiz Lib提供了一套简单易用的事件转发功能，允许游戏中和实体相关的事件被转发到实体保有的事件处理器处，
从而避免繁琐的事件注册与动态增减。

这一逻辑是：使用`AttachmentType`用于存储针对某一事件的处理器列表；
监听与实体有关的事件，从事件中获取该`数据附加`并执行对应事件包含的处理器，例如：

```java
public static final Consumer<EntityEvent> consumer = event -> event.getEntity().getExistingData(Horiz.EVENT_DISTRIBUTE).ifPresent(d -> d.post(event));

@SubscribeEvent
public static void hurtIncome(LivingIncomingDamageEvent event) {
    consumer.accept(event);
}
```

这一过程可以使得针对单个实体的，可变的事件处理行为变得更加灵活而便捷。
您可以使用这一过程实现类似“匠魂装备词条” “饰品效果提供” “实体技能系统”等功能。

# 如何使用该系统？

首先我们需要拿到对应的服务端`Entity`。在获取到目标实体后，您就可以使用：

```java
public <T extends Event> void initEntity() {
    Entity entity;//你获取到的实体
    Class<T> eventClazz;//需要监听的事件的类
    Consumer<T> eventConsumer;//该事件的处理器

    //IdentEvent包含了事件的类与其对应的处理器，用于管理添加的监听
    IdentEvent<T> ident = new IdentEvent(eventClazz, eventConsumer);

    EntityEventDistribute distribute = entity.getData(Horiz.EVENT_DISTRIBUTE);

    distribute.add(ident);//使用add方法添加一个新的监听
    distribute.remove(ident);//使用remove方法清除存在的监听
    distribute.removeByClass(eventClazz);//或者使用removeByClass清除某一事件的所有监听

    //注：作用域为该实体分发器内的监听器，不影响游戏事件总线的注册与其它实体的分发器
}
```

此外，您可以通过为事件添加标记来在需要时快速移除它们。

想象一个文件夹——您可以往里面添加文件，也可以添加其它文件夹。标记的设计也是如此，
在[`EntityEventDistribute`](src/main/java/com/phasetranscrystal/horiz/EntityEventDistribute.java)的`add`方法中，
允许在`IdentEvent`后继续添加一组`ResourceLocation`作为监听器的标记路径，而在移除，即`removeMarked`方法处，可以根据这一路径快捷移除监听器。

让我们用一个例子来展示：

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
    //这时候存储的结构大概是这样的：
    //root
    //|-(i1),(i2)
    //|-loc1
    //| |-(i3)
    //| |-loc2
    //| |  \(i4),(i5)
    //| \loc3
    //|   |-(i6)
    //|   \loc4
    //|     \(i7)
    //\loc2
    //  |-(i8)
    //  \loc3
    //    \(i9)

    //假定下面的方法执行后distribute自动还原（实际上不会这样）
    distribute.removeMarked(loc1, loc3, loc4); //移除i7
    distribute.removeMarked(loc1, loc3); //移除i6,i7
    distribute.removeMarked(loc1, loc2); //移除i4,i5
    distribute.removeMarked(loc1); //移除i3,i4,i5,i6,i7
    distribute.removeMarkedSelf(loc1); //移除i3，不移除子路径下的内容
    distribute.removeMarked(loc2); //移除i8,i9
}
```

# 添加更多事件转发

正如我们在最开始提到的，`EventConsumer#consumer`常量内完成了转发事件的逻辑。您只需要监听已有事件，并使用`Consumer#accept`方法执行即可。
这一过程简化的是获取实体数据附加的过程。如果您的事件不是`EntityEvent`，您可能需要自己完成相应逻辑。

# 其它问题

## 我应该在何时初始化转发的事件监听器？

您可以在实体创建时直接初始化。如果您无法做到或需要对各种实体进行全局初始化，
您可以监听[`GatherEntityDistributeEvent`](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java)事件，
该事件转发自实体加入世界的最低优先级。

注意：由于该数据附加没有配置`Codec`，转发中的事件不会被保存——这意味着，当实体重新加载或传送到其它维度时，您需要重新初始化事件。
因此我们更推荐使用`GatherEntityDistributeEvent`。

## 我想处理实体对目标造成攻击/实体杀死目标等情况，我应该怎么办？

库提供了伤害事件和击杀事件的转发——您可以在[`EventConsumer类的底部`](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java)
找到其转发对应的事件。

## 模组为已经为哪些事件提供了转发？

您可以在[`EventConsumer#bootstrapConsumer()`](src/main/java/com/phasetranscrystal/horiz/EventConsumer.java)
下看到模组已经提供的转发。

## 如果同一个事件被不同模组转发，监听器会不会被执行多次？

不会。 在[`EntityEventDistribute`](src/main/java/com/phasetranscrystal/horiz/EntityEventDistribute.java)
类中保有一个记录事件哈希值的表，如果同一个事件的相同实例被多次转发，在第一次之后的转发均会被过滤。

# 贡献

欢迎对Horiz模组进行贡献。你可以提交Issues来报告问题，或者Fork本项目来提交你的改进。

# 开发者

- **Mon-landis**：提供技术支持和开发。

---

