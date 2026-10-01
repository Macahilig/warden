package com.example.wardenautohit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public class WardenAutoHitClient implements ClientModInitializer {

    // toggled with a keybind so it isn't always running
    private static boolean enabled = false;
    private static KeyBinding toggleKey;

    // reach used for the raycast, in blocks
    private static final double REACH = 4.0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.warden-autohit.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.warden-autohit"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("[WardenAutoHit] " + (enabled ? "ON" : "OFF")),
                            true
                    );
                }
            }

            if (enabled) {
                tick(client);
            }
        });
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) return;

        PlayerEntity player = client.player;
        Entity warden = raycastWarden(player);
        if (warden == null) return;

        // wait for the vanilla attack cooldown so it doesn't spam through the swing animation
        if (player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        client.interactionManager.attackEntity(player, warden);
        player.swingHand(Hand.MAIN_HAND);
    }

    private static Entity raycastWarden(PlayerEntity player) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(REACH));

        Box searchBox = player.getBoundingBox().stretch(look.multiply(REACH)).expand(1.0);

        Predicate<Entity> predicate = e -> e instanceof WardenEntity && e.isAlive() && !e.isSpectator();

        EntityHitResult result = ProjectileUtil.raycast(player, start, end, searchBox, predicate, REACH * REACH);

        if (result != null && result.getType() == HitResult.Type.ENTITY) {
            return result.getEntity();
        }
        return null;
    }
}
