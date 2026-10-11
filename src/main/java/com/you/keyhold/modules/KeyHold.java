package com.you.keyhold.modules;

import com.you.keyhold.KeyHoldAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class KeyHold extends Module {
    private final SettingGroup sgKeys = this.settings.createGroup("Keys");
    private final SettingGroup sgMouse = this.settings.createGroup("Mouse");
    private final SettingGroup sgPeriodic = this.settings.createGroup("Periodic Attack");
    private final SettingGroup sgPeriodicUse = this.settings.createGroup("Periodic Use");

    private final Setting<Keybind> key1 = sgKeys.add(new KeybindSetting.Builder()
        .name("key-1").description("First key to hold down.").defaultValue(Keybind.none()).build()
    );
    private final Setting<Keybind> key2 = sgKeys.add(new KeybindSetting.Builder()
        .name("key-2").description("Second key to hold down.").defaultValue(Keybind.none()).build()
    );
    private final Setting<Keybind> key3 = sgKeys.add(new KeybindSetting.Builder()
        .name("key-3").description("Third key to hold down.").defaultValue(Keybind.none()).build()
    );
    private final Setting<Keybind> key4 = sgKeys.add(new KeybindSetting.Builder()
        .name("key-4").description("Fourth key to hold down.").defaultValue(Keybind.none()).build()
    );

    private final Setting<Boolean> holdLeft = sgMouse.add(new BoolSetting.Builder()
        .name("hold-left").description("Hold left click.").defaultValue(false).build()
    );
    private final Setting<Boolean> holdRight = sgMouse.add(new BoolSetting.Builder()
        .name("hold-right").description("Hold right click.").defaultValue(false).build()
    );
    private final Setting<Boolean> holdMiddle = sgMouse.add(new BoolSetting.Builder()
        .name("hold-middle").description("Hold middle click.").defaultValue(false).build()
    );

    private final Setting<Boolean> periodicEnabled = sgPeriodic.add(new BoolSetting.Builder()
        .name("periodic-attack").description("Click attack once per interval instead of holding.").defaultValue(false).build()
    );
    private final Setting<Integer> periodicInterval = sgPeriodic.add(new IntSetting.Builder()
        .name("attack-interval").description("Attack interval in ticks (1 tick = 50ms).").defaultValue(13).min(1).max(200).sliderMax(200).build()
    );

    private final Setting<Boolean> periodicUseEnabled = sgPeriodicUse.add(new BoolSetting.Builder()
        .name("periodic-use").description("Click use once per interval instead of holding.").defaultValue(false).build()
    );
    private final Setting<Integer> useInterval = sgPeriodicUse.add(new IntSetting.Builder()
        .name("use-interval").description("Use interval in ticks (1 tick = 50ms).").defaultValue(13).min(1).max(40).sliderMax(40).build()
    );

    private final List<KeyBinding> trackedBindings = new ArrayList<>();
    private int attackTickCounter = 0;
    private int useTickCounter = 0;

    public KeyHold() {
        super(KeyHoldAddon.CATEGORY, "key-hold", "Holds down selected keys and mouse buttons continuously.");
    }

    @Override
    public void onActivate() {
        trackedBindings.clear();
        attackTickCounter = 0;
        useTickCounter = 0;
    }

    @Override
    public void onDeactivate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        for (KeyBinding kb : trackedBindings) {
            kb.setPressed(false);
            drainTimesPressed(kb);
        }
        trackedBindings.clear();
        attackTickCounter = 0;
        useTickCounter = 0;

        // Always release mouse keys unconditionally — settings may have changed while active
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
        mc.options.pickItemKey.setPressed(false);
        drainTimesPressed(mc.options.attackKey);
        drainTimesPressed(mc.options.useKey);
        drainTimesPressed(mc.options.pickItemKey);

        // No player.input restore needed — we never replace it anymore.
    }

    /**
     * Drains the KeyBinding's internal timesPressed counter so no phantom
     * "wasPressed" events fire in the tick after we release the key.
     * setPressed(false) only clears the 'held' boolean; GLFW callbacks may
     * have incremented timesPressed independently, which Minecraft consumes
     * via wasPressed() and treats as fresh key taps even after we let go.
     */
    private static void drainTimesPressed(KeyBinding kb) {
        //noinspection StatementWithEmptyBody
        while (kb.wasPressed()) { /* discard */ }
    }

    public void holdTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        trackedBindings.clear();

        collectBindings(mc, key1.get());
        collectBindings(mc, key2.get());
        collectBindings(mc, key3.get());
        collectBindings(mc, key4.get());

        for (KeyBinding kb : trackedBindings) {
            kb.setPressed(true);
        }

        if (periodicEnabled.get() && !holdLeft.get()) {
            attackTickCounter++;
            if (attackTickCounter >= periodicInterval.get()) {
                attackTickCounter = 0;
                performPeriodicAttack(mc);
            }
        } else if (holdLeft.get()) {
            mc.options.attackKey.setPressed(true);
        }

        if (periodicUseEnabled.get() && !holdRight.get()) {
            useTickCounter++;
            if (useTickCounter >= useInterval.get()) {
                useTickCounter = 0;
                performPeriodicUse(mc);
            }
        } else if (holdRight.get()) {
            mc.options.useKey.setPressed(true);
        }
        if (holdMiddle.get()) mc.options.pickItemKey.setPressed(true);

        // No need to replace mc.player.input — vanilla's KeyboardInput.tick() already
        // reads isPressed() from the KeyBinding objects we set above, so movement keys
        // held via setPressed(true) work through the normal input path.
    }

    private void performPeriodicAttack(MinecraftClient mc) {
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (mc.player.isUsingItem()) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void performPeriodicUse(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null && mc.player != null) {
            hit = mc.player.raycast(5.0, 1.0F, false);
        }

        if (hit != null && hit.getType() == HitResult.Type.BLOCK && !mc.options.sneakKey.isPressed()) {
            net.minecraft.util.ActionResult blockResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult) hit);
            if (blockResult.isAccepted()) return;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private void collectBindings(MinecraftClient mc, Keybind keybind) {
        if (keybind == null || !keybind.isSet()) return;

        InputUtil.Key targetKey = keybind.isKey()
            ? InputUtil.Type.KEYSYM.createFromCode(keybind.getValue())
            : InputUtil.Type.MOUSE.createFromCode(keybind.getValue());

        for (KeyBinding kb : mc.options.allKeys) {
            // Use getBoundKey() (the player's current binding) not getDefaultKey()
            if (((com.you.keyhold.mixin.KeyBindingAccessor) kb).getBoundKey().equals(targetKey)) {
                trackedBindings.add(kb);
            }
        }
    }
}
