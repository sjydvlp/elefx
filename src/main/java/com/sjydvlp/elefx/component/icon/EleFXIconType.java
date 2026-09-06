package com.sjydvlp.elefx.component.icon;

/**
 * 内置 SVG 图标类型。
 *
 * <p>
 * 每个图标均对应一个可直接预览的标准 SVG 资源文件，坐标系为
 * {@code 0 0 24 24}。
 * </p>
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public enum EleFXIconType {

    ADD("add", "svg/add.svg"),
    REMOVE("remove", "svg/remove.svg"),
    CLOSE("close", "svg/close.svg"),
    CHECK("check", "svg/check.svg"),
    SEARCH("search", "svg/search.svg"),
    MENU("menu", "svg/menu.svg"),
    ARROW_LEFT("arrow-left", "svg/arrow-left.svg"),
    ARROW_RIGHT("arrow-right", "svg/arrow-right.svg"),
    ARROW_UP("arrow-up", "svg/arrow-up.svg"),
    ARROW_DOWN("arrow-down", "svg/arrow-down.svg"),
    CHEVRON_LEFT("chevron-left", "svg/chevron-left.svg"),
    CHEVRON_RIGHT("chevron-right", "svg/chevron-right.svg"),
    CHEVRON_UP("chevron-up", "svg/chevron-up.svg"),
    CHEVRON_DOWN("chevron-down", "svg/chevron-down.svg"),
    MORE_HORIZONTAL("more-horizontal", "svg/more-horizontal.svg"),
    MORE_VERTICAL("more-vertical", "svg/more-vertical.svg"),

    HOME("home", "svg/home.svg"),
    USER("user", "svg/user.svg"),
    CALENDAR("calendar", "svg/calendar.svg"),
    CLOCK("clock", "svg/clock.svg"),
    EDIT("edit", "svg/edit.svg"),
    DELETE("delete", "svg/delete.svg"),
    COPY("copy", "svg/copy.svg"),
    DOWNLOAD("download", "svg/download.svg"),
    UPLOAD("upload", "svg/upload.svg"),
    REFRESH("refresh", "svg/refresh.svg"),
    FILTER("filter", "svg/filter.svg"),
    SORT("sort", "svg/sort.svg"),
    SHARE("share", "svg/share.svg"),

    INFO("info", "svg/info.svg"),
    INFO_FILLED("info-filled", "svg/info-filled.svg"),
    SUCCESS("success", "svg/success.svg"),
    WARNING("warning", "svg/warning.svg"),
    ERROR("error", "svg/error.svg"),
    QUESTION("question", "svg/question.svg"),

    STAR("star", "svg/star.svg"),
    HEART("heart", "svg/heart.svg"),
    EYE("eye", "svg/eye.svg"),
    LOCK("lock", "svg/lock.svg"),
    UNLOCK("unlock", "svg/unlock.svg"),
    FOLDER("folder", "svg/folder.svg"),
    FILE("file", "svg/file.svg"),
    IMAGE("image", "svg/image.svg"),
    CAMERA("camera", "svg/camera.svg"),
    PLAY("play", "svg/play.svg"),
    PAUSE("pause", "svg/pause.svg"),
    LOCATION("location", "svg/location.svg"),
    PHONE("phone", "svg/phone.svg"),
    MAIL("mail", "svg/mail.svg"),
    BELL("bell", "svg/bell.svg"),
    LINK("link", "svg/link.svg"),
    EXTERNAL_LINK("external-link", "svg/external-link.svg");

    private final String name;

    private final String path;

    EleFXIconType(String name, String path) {
        this.name = name;
        this.path = path;
    }

    /**
     * Returns the icon's stable, kebab-case name.
     *
     * @return the icon name
     */
    public String iconName() {
        return name;
    }

    /**
     * Returns the classpath location of this icon's source SVG file.
     *
     * @return an absolute classpath resource path
     */
    public String path() {
        return path;
    }

    /**
     * Returns the style class associated with this icon type.
     *
     * @return an icon-specific CSS style class
     */
    public String styleClass() {
        return "ele-icon--" + name;
    }

    /**
     * Returns the classpath location of this icon's source SVG file.
     *
     * @return an absolute classpath resource path
     */
    public String svgResourcePath() {
        return path();
    }

    /**
     * Loads the SVG path {@code d} attribute used by JavaFX.
     *
     * @return SVG path data in a 24 by 24 coordinate system
     */
    public String svgPathData() {
        return EleFXIconResources.pathData(this);
    }
}
