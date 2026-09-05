package com.sjydvlp.elefx.component.icon;

/**
 * An SVG icon definition shared by the filled and outlined icon collections.
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public interface EleFXIconType {

    /**
     * Returns the icon's stable, kebab-case name.
     *
     * @return the icon name
     */
    String iconName();

    /**
     * Returns the absolute classpath location of the source SVG file.
     *
     * @return the SVG resource path
     */
    String path();

    /**
     * Returns whether this definition belongs to the filled icon collection.
     *
     * @return {@code true} for a filled icon, otherwise {@code false}
     */
    boolean isFilled();

    /**
     * Returns the style class associated with this icon's semantic name.
     *
     * @return an icon-specific CSS style class
     */
    default String styleClass() {
        return "ele-icon--" + iconName();
    }

    /**
     * Returns the classpath location of this icon's source SVG file.
     *
     * @return an absolute classpath resource path
     */
    default String svgResourcePath() {
        return path();
    }

    /**
     * Loads the SVG path {@code d} attribute used by JavaFX.
     *
     * @return SVG path data in a 24 by 24 coordinate system
     */
    default String svgPathData() {
        return EleFXIconResources.pathData(this);
    }
}
