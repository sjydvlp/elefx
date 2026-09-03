package com.sjydvlp.elefx.theme;

import javafx.scene.Parent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 可主题的
 *
 * @author sjydvlp@163.com
 * @date 2026/8/18 23:45
 */
public interface Themable {

    Parent toParent();

    Theme getTheme();

    default boolean sceneBuilderIntegration() {
        if (!SceneBuilderIntegration.isInSceneBuilder() || Helper.isInhibitSBSupport()) {
            return false;
        }

        Helper.themeIt(this);
        return true;
    }

    class Helper {

        public static final Path SB_WIN_PATH = Path.of(System.getenv("APPDATA") + "/Scene Builder");

        public static final Path SB_MAC_PATH = Path
                .of(System.getProperty("user.home") + "/Library/Application Support/Scene Builder");

        public static final Path SB_LIN_PATH = Path.of(System.getProperty("user.home") + "/.scenebuilder");

        private static OSType os = null;

        private static Boolean inhibitSBSupport = null;

        public enum OSType {
            Windows,
            MacOS,
            Linux,
            Other
        }

        protected static void themeIt(Themable t) {
            Parent parent = t.toParent();
            Set<String> stylesheets = new HashSet<>(parent.getStylesheets());
            String theme = t.getTheme().toData();
            if (stylesheets.contains(theme)) {
                return;
            }
            parent.getStylesheets().add(theme);
        }

        protected static OSType detectOS() {
            if (os == null) {
                String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
                if ((OS.contains("mac")) || (OS.contains("darwin"))) {
                    os = OSType.MacOS;
                } else if (OS.contains("win")) {
                    os = OSType.Windows;
                } else if (OS.contains("nux")) {
                    os = OSType.Linux;
                } else {
                    os = OSType.Other;
                }
            }
            return os;
        }

        protected static boolean isInhibitSBSupport() {
            if (inhibitSBSupport == null) {
                switch (detectOS()) {
                    case Windows: {
                        inhibitSBSupport = Files.exists(SB_WIN_PATH.resolve("MFX_SB_OFF"));
                        break;
                    }
                    case MacOS: {
                        inhibitSBSupport = Files.exists(SB_MAC_PATH.resolve("MFX_SB_OFF"));
                        break;
                    }
                    case Linux: {
                        inhibitSBSupport = Files.exists(SB_LIN_PATH.resolve("MFX_SB_OFF"));
                        break;
                    }
                    default: {
                        inhibitSBSupport = false;
                        break;
                    }
                }
            }
            return inhibitSBSupport;
        }
    }
}
