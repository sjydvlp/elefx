package com.sjydvlp.elefx.component.icon;

/**
 * 内置实心 SVG 图标类型。
 *
 * <p>
 * 每个图标均对应一个可直接预览的标准 SVG 资源文件，坐标系为
 * {@code 0 0 24 24}。
 * </p>
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public enum EleFXFilledIconType implements EleFXIconType {

    ADD("add", "/com/sjydvlp/elefx/svg/icon/filled/add.svg"),
    REMOVE("remove", "/com/sjydvlp/elefx/svg/icon/filled/remove.svg"),
    CLOSE("close", "/com/sjydvlp/elefx/svg/icon/filled/close.svg"),
    CHECK("check", "/com/sjydvlp/elefx/svg/icon/filled/check.svg"),
    SEARCH("search", "/com/sjydvlp/elefx/svg/icon/filled/search.svg"),
    MENU("menu", "/com/sjydvlp/elefx/svg/icon/filled/menu.svg"),
    ARROW_LEFT("arrow-left", "/com/sjydvlp/elefx/svg/icon/filled/arrow-left.svg"),
    ARROW_RIGHT("arrow-right", "/com/sjydvlp/elefx/svg/icon/filled/arrow-right.svg"),
    ARROW_UP("arrow-up", "/com/sjydvlp/elefx/svg/icon/filled/arrow-up.svg"),
    ARROW_DOWN("arrow-down", "/com/sjydvlp/elefx/svg/icon/filled/arrow-down.svg"),
    CHEVRON_LEFT("chevron-left", "/com/sjydvlp/elefx/svg/icon/filled/chevron-left.svg"),
    CHEVRON_RIGHT("chevron-right", "/com/sjydvlp/elefx/svg/icon/filled/chevron-right.svg"),
    CHEVRON_UP("chevron-up", "/com/sjydvlp/elefx/svg/icon/filled/chevron-up.svg"),
    CHEVRON_DOWN("chevron-down", "/com/sjydvlp/elefx/svg/icon/filled/chevron-down.svg"),
    MORE_HORIZONTAL("more-horizontal", "/com/sjydvlp/elefx/svg/icon/filled/more-horizontal.svg"),
    MORE_VERTICAL("more-vertical", "/com/sjydvlp/elefx/svg/icon/filled/more-vertical.svg"),

    HOME("home", "/com/sjydvlp/elefx/svg/icon/filled/home.svg"),
    USER("user", "/com/sjydvlp/elefx/svg/icon/filled/user.svg"),
    CALENDAR("calendar", "/com/sjydvlp/elefx/svg/icon/filled/calendar.svg"),
    CLOCK("clock", "/com/sjydvlp/elefx/svg/icon/filled/clock.svg"),
    EDIT("edit", "/com/sjydvlp/elefx/svg/icon/filled/edit.svg"),
    DELETE("delete", "/com/sjydvlp/elefx/svg/icon/filled/delete.svg"),
    COPY("copy", "/com/sjydvlp/elefx/svg/icon/filled/copy.svg"),
    DOWNLOAD("download", "/com/sjydvlp/elefx/svg/icon/filled/download.svg"),
    UPLOAD("upload", "/com/sjydvlp/elefx/svg/icon/filled/upload.svg"),
    REFRESH("refresh", "/com/sjydvlp/elefx/svg/icon/filled/refresh.svg"),
    FILTER("filter", "/com/sjydvlp/elefx/svg/icon/filled/filter.svg"),
    SORT("sort", "/com/sjydvlp/elefx/svg/icon/filled/sort.svg"),
    SHARE("share", "/com/sjydvlp/elefx/svg/icon/filled/share.svg"),

    INFO("info", "/com/sjydvlp/elefx/svg/icon/filled/info.svg"),
    INFO_FILLED("info-filled", "/com/sjydvlp/elefx/svg/icon/filled/info-filled.svg"),
    SUCCESS("success", "/com/sjydvlp/elefx/svg/icon/filled/success.svg"),
    WARNING("warning", "/com/sjydvlp/elefx/svg/icon/filled/warning.svg"),
    ERROR("error", "/com/sjydvlp/elefx/svg/icon/filled/error.svg"),
    QUESTION("question", "/com/sjydvlp/elefx/svg/icon/filled/question.svg"),

    STAR("star", "/com/sjydvlp/elefx/svg/icon/filled/star.svg"),
    HEART("heart", "/com/sjydvlp/elefx/svg/icon/filled/heart.svg"),
    EYE("eye", "/com/sjydvlp/elefx/svg/icon/filled/eye.svg"),
    LOCK("lock", "/com/sjydvlp/elefx/svg/icon/filled/lock.svg"),
    UNLOCK("unlock", "/com/sjydvlp/elefx/svg/icon/filled/unlock.svg"),
    FOLDER("folder", "/com/sjydvlp/elefx/svg/icon/filled/folder.svg"),
    FILE("file", "/com/sjydvlp/elefx/svg/icon/filled/file.svg"),
    IMAGE("image", "/com/sjydvlp/elefx/svg/icon/filled/image.svg"),
    CAMERA("camera", "/com/sjydvlp/elefx/svg/icon/filled/camera.svg"),
    PLAY("play", "/com/sjydvlp/elefx/svg/icon/filled/play.svg"),
    PAUSE("pause", "/com/sjydvlp/elefx/svg/icon/filled/pause.svg"),
    LOCATION("location", "/com/sjydvlp/elefx/svg/icon/filled/location.svg"),
    PHONE("phone", "/com/sjydvlp/elefx/svg/icon/filled/phone.svg"),
    MAIL("mail", "/com/sjydvlp/elefx/svg/icon/filled/mail.svg"),
    BELL("bell", "/com/sjydvlp/elefx/svg/icon/filled/bell.svg"),
    LINK("link", "/com/sjydvlp/elefx/svg/icon/filled/link.svg"),
    EXTERNAL_LINK("external-link", "/com/sjydvlp/elefx/svg/icon/filled/external-link.svg");

    private final String name;

    private final String path;

    EleFXFilledIconType(String name, String path) {
        this.name = name;
        this.path = path;
    }

    /**
     * Returns the icon's stable, kebab-case name.
     *
     * @return the icon name
     */
    @Override
    public String iconName() {
        return name;
    }

    /**
     * Returns the classpath location of this icon's source SVG file.
     *
     * @return an absolute classpath resource path
     */
    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean isFilled() {
        return true;
    }
}
