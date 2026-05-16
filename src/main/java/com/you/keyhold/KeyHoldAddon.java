package com.you.keyhold;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.you.keyhold.modules.KeyHold;
import org.slf4j.Logger;

public class KeyHoldAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("KeyHold");

    @Override
    public void onInitialize() {
        LOG.info("Initializing KeyHold addon");
        Modules.get().add(new KeyHold());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.you.keyhold";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("you", "meteor-hold-addon");
    }
}
