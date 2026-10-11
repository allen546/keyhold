package com.you.keyhold.mixin;

import com.you.keyhold.modules.KeyHold;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientAccessor {

    @Inject(method = "tick", at = @At("TAIL"))
    private void keyhold$onTickTail(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null || mc.world == null) return;

        Modules modules = Modules.get();
        if (modules == null) return;
        KeyHold keyHold = modules.get(KeyHold.class);
        if (keyHold == null || !keyHold.isActive()) return;

        // Temporarily hide the current screen so Minecraft's input logic
        // doesn't treat our synthetic key presses as GUI events.
        Screen savedScreen = mc.currentScreen;
        mc.currentScreen = null;
        keyHold.holdTick(mc);
        mc.currentScreen = savedScreen;
    }
}
