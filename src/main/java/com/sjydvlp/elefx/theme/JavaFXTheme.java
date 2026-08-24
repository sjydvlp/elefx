package com.sjydvlp.elefx.theme;

import cn.hutool.core.io.resource.ResourceUtil;

import java.io.InputStream;

public enum JavaFXTheme implements Theme {

    MODENA("css/jfx/modena.css");

    private final String path;

    JavaFXTheme(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public InputStream assets() {
        String path = path().substring(0, path().lastIndexOf("/") + 1) + "assets.zip";
        return ResourceUtil.getStream(path);
    }
}
