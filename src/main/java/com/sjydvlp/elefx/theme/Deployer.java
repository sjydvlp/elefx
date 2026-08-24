package com.sjydvlp.elefx.theme;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Deployer {

    private static final Deployer instance = new Deployer();

    public static Deployer instance() {
        return instance;
    }

    private final Map<String, Map<String, Path>> cache = new HashMap<>();

    private final Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"), "themes-assets");

    private Deployer() {
    }

    public void deploy(Theme theme) throws Exception {
        if (!Files.isDirectory(tmpDir)) {
            Files.createDirectories(tmpDir);
        }

        Path zipPath = null;
        try (InputStream in = theme.assets()) {
            if (in == null) {
                return;
            }

            Path destDir = tmpDir.resolve(theme.deployName());
            if (!Files.isDirectory(destDir)) {
                Files.createDirectories(destDir);
            }

            // Copy zip to file system
            zipPath = destDir.resolve("assets.zip");
            OutputStream out = Files.newOutputStream(zipPath);
            in.transferTo(out);

            // Unzip
            try (ZipFile zf = new ZipFile(zipPath.toFile())) {
                List<? extends ZipEntry> entries = zf.stream().collect(Collectors.toList());
                for (ZipEntry entry : entries) {
                    unzip(theme, zf, entry, destDir);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (zipPath != null) Files.delete(zipPath);
        }
    }

    public boolean clean(Theme theme) {
        Map<String, Path> assets = cache.remove(theme.deployName());
        if (assets == null) return true;
        try {
            for (Path path : assets.values()) {
                delete(path);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean cleanAll() {
        try {
            if (Files.isDirectory(tmpDir)) delete(tmpDir);
            cache.clear();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public Map<String, Path> getDeployed(Theme theme) {
        return cache.get(theme.deployName());
    }

    private void unzip(Theme theme, ZipFile zf, ZipEntry entry, Path destDir) throws IOException {
        String name = entry.getName();
        Path target = destDir.resolve(name);
        if (entry.isDirectory()) {
            Files.createDirectories(target);
        } else {
            try (InputStream in = zf.getInputStream(entry)) {
                Files.copy(in, target, REPLACE_EXISTING);
                Map<String, Path> themeCache = cache.computeIfAbsent(theme.deployName(), t -> new HashMap<>());
                themeCache.put(name, target);
            }
        }
    }

    private void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }
}
