package com.you.keyhold.mixin;

import com.you.keyhold.modules.KeyHold;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientAccessor {
    @Unique
    private Screen keyhold$savedScreen;

    @Inject(method = "tick", at = @At("HEAD"))
    private void keyhold$onTickHead(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null || mc.world == null) return;

        Modules modules = Modules.get();
        if (modules == null) return;
        KeyHold keyHold = modules.get(KeyHold.class);
        if (keyHold == null || !keyHold.isActive()) return;

        keyHold.holdTick(mc);

        keyhold$savedScreen = mc.currentScreen;
        mc.currentScreen = null;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void keyhold$onTickTail(CallbackInfo ci) {
        if (keyhold$savedScreen != null) {
            MinecraftClient mc = (MinecraftClient) (Object) this;
            mc.currentScreen = keyhold$savedScreen;
            keyhold$savedScreen = null;
        }
    }
}
