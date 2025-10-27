package com.phasetranscrystal.horiz;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Horiz.MODID)
public class Horiz {

    public static final String MODID = "horiz";

    public Horiz(IEventBus bus) {
        REGISTER.register(bus);
        EventConsumer.bootstrapConsumer();
    }

    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    // 我们不需要保存它，因此也没有codec。
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityEventDistribute>> EVENT_DISTRIBUTE = REGISTER.register("event_dispatch", () -> AttachmentType.builder(EntityEventDistribute::new).build());
}
