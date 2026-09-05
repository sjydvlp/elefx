package com.sjydvlp.elefx.component.icon;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Internal SVG resource loader. */
final class EleFXIconResources {

    private static final Pattern PATH_DATA_PATTERN = Pattern.compile(
            "<path\\b[^>]*\\bd\\s*=\\s*\\\"([^\\\"]+)\\\"", Pattern.CASE_INSENSITIVE);

    private static final Map<EleFXIconType, String> PATH_DATA_CACHE = new ConcurrentHashMap<>();

    private EleFXIconResources() {
    }

    static String pathData(EleFXIconType type) {
        return PATH_DATA_CACHE.computeIfAbsent(type, EleFXIconResources::readPathData);
    }

    private static String readPathData(EleFXIconType type) {
        String resourcePath = type.svgResourcePath();
        try (InputStream stream = EleFXIconResources.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("SVG icon resource does not exist: " + resourcePath);
            }

            String svg = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = PATH_DATA_PATTERN.matcher(svg);
            if (!matcher.find()) {
                throw new IllegalStateException("SVG icon resource has no path data: " + resourcePath);
            }
            return matcher.group(1);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read SVG icon resource: " + resourcePath, exception);
        }
    }
}
