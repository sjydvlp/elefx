package com.sjydvlp.elefx.component.icon;

/**
 * EleFX 内置 SVG 图标的便捷工厂。
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public final class EleFXIcons {

    private EleFXIcons() {
    }

    /**
     * Creates a default-size icon.
     *
     * @param type the icon type
     * @return a new 16px icon
     */
    public static EleFXIcon of(EleFXIconType type) {
        return new EleFXIcon(type);
    }

    /**
     * Creates an icon at the requested size.
     *
     * @param type the icon type
     * @param size icon size in pixels
     * @return a new icon
     */
    public static EleFXIcon of(EleFXIconType type, double size) {
        return new EleFXIcon(type, size);
    }
}
