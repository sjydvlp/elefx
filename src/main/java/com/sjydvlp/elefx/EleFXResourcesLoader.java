package com.sjydvlp.elefx;

import java.io.InputStream;
import java.net.URL;

public class EleFXResourcesLoader {

    public static URL loadURL(String path) {
        return EleFXResourcesLoader.class.getResource(path);
    }

    public static String load(String path) {
        return loadURL(path).toString();
    }

    public static InputStream loadStream(String name) {
        return EleFXResourcesLoader.class.getResourceAsStream(name);
    }

    private EleFXResourcesLoader() {
    }
}
