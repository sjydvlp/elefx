package com.sjydvlp.elefx.theme;

import cn.hutool.core.io.resource.ResourceUtil;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public interface Theme {

    String name();

    String path();

    default URL get() {
        if (Helper.isCached(this) && Helper.getCachedTheme(this) != null) {
            return Helper.getCachedTheme(this);
        }
        return Helper.cacheTheme(this, ResourceUtil.getResource(path()));
    }

    default String toData() {
        return get().toExternalForm();
    }

    default void applyGlobal() {
        Application.setUserAgentStylesheet(toData());
    }

    default void applyOn(Scene scene) {
        scene.getStylesheets().add(toData());
    }

    default void applyOn(Parent parent) {
        parent.getStylesheets().add(toData());
    }

    default InputStream assets() {
        return null;
    }

    default void deploy() {
        try {
            Deployer.instance().deploy(this);
        } catch (Exception ex) {
            System.err.println("Failed to deploy theme: " + name() + ", because: " + ex.getMessage());
        }
    }

    default String deployName() {
        return name().toLowerCase();
    }

    default void clean() {
        Deployer.instance().clean(this);
    }

    default boolean isDeployed() {
        return Deployer.instance().getDeployed(this) != null;
    }

    class Helper {

        private static final Map<Theme, URL> CACHE = new HashMap<>();

        public static boolean isCached(Theme theme) {
            return CACHE.containsKey(theme);
        }

        public static URL cacheTheme(Theme theme, URL url) {
            CACHE.put(theme, url);
            return url;
        }

        public static URL getCachedTheme(Theme theme) {
            return CACHE.get(theme);
        }
    }
}
