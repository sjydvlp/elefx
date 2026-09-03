package com.sjydvlp.elefx.theme;

import com.sjydvlp.elefx.EleFXResourcesLoader;

import java.io.InputStream;

public enum JavaFXThemes implements Theme {

    MODENA("css/jfx/modena.css");

    private final String path;

    JavaFXThemes(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public InputStream assets() {
        return EleFXResourcesLoader.loadStream("css/jfx/assets.zip");
    }
}
