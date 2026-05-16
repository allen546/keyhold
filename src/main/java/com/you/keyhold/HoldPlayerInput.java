package com.you.keyhold;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

public class HoldPlayerInput extends Input {
    private final MinecraftClient mc;

    public HoldPlayerInput(MinecraftClient mc) {
        this.mc = mc;
    }

    @Override
    public void tick() {
        boolean forward = isHeld(mc.options.forwardKey);
        boolean backward = isHeld(mc.options.backKey);
        boolean left = isHeld(mc.options.leftKey);
        boolean right = isHeld(mc.options.rightKey);
        boolean jump = isHeld(mc.options.jumpKey);
        boolean sneak = isHeld(mc.options.sneakKey);
        boolean sprint = isHeld(mc.options.sprintKey);

        this.playerInput = new PlayerInput(forward, backward, left, right, jump, sneak, sprint);

        float forwardMultiplier = getMovementMultiplier(forward, backward);
        float sidewaysMultiplier = getMovementMultiplier(left, right);
        this.movementVector = new Vec2f(sidewaysMultiplier, forwardMultiplier).normalize();
    }

    private static boolean isHeld(KeyBinding key) {
        return key != null && key.isPressed();
    }

    private static float getMovementMultiplier(boolean pressed, boolean oppositePressed) {
        if (pressed == oppositePressed) return 0.0f;
        return pressed ? 1.0f : -1.0f;
    }
}
