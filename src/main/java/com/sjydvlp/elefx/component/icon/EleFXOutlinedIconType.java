package com.sjydvlp.elefx.component.icon;

/**
 * 内置描边 SVG 图标类型。
 *
 * <p>
 * 每个图标均对应一个可直接预览的标准 SVG 资源文件，坐标系为
 * {@code 0 0 24 24}。
 * </p>
 *
 * @author sjydvlp@163.com
 * @date 2026/9/5
 */
public enum EleFXOutlinedIconType implements EleFXIconType {

    ADD("add", "/com/sjydvlp/elefx/svg/icon/outlined/add.svg"),
    REMOVE("remove", "/com/sjydvlp/elefx/svg/icon/outlined/remove.svg"),
    CLOSE("close", "/com/sjydvlp/elefx/svg/icon/outlined/close.svg"),
    CHECK("check", "/com/sjydvlp/elefx/svg/icon/outlined/check.svg"),
    SEARCH("search", "/com/sjydvlp/elefx/svg/icon/outlined/search.svg"),
    MENU("menu", "/com/sjydvlp/elefx/svg/icon/outlined/menu.svg"),
    ARROW_LEFT("arrow-left", "/com/sjydvlp/elefx/svg/icon/outlined/arrow-left.svg"),
    ARROW_RIGHT("arrow-right", "/com/sjydvlp/elefx/svg/icon/outlined/arrow-right.svg"),
    ARROW_UP("arrow-up", "/com/sjydvlp/elefx/svg/icon/outlined/arrow-up.svg"),
    ARROW_DOWN("arrow-down", "/com/sjydvlp/elefx/svg/icon/outlined/arrow-down.svg"),
    CHEVRON_LEFT("chevron-left", "/com/sjydvlp/elefx/svg/icon/outlined/chevron-left.svg"),
    CHEVRON_RIGHT("chevron-right", "/com/sjydvlp/elefx/svg/icon/outlined/chevron-right.svg"),
    CHEVRON_UP("chevron-up", "/com/sjydvlp/elefx/svg/icon/outlined/chevron-up.svg"),
    CHEVRON_DOWN("chevron-down", "/com/sjydvlp/elefx/svg/icon/outlined/chevron-down.svg"),
    MORE_HORIZONTAL("more-horizontal", "/com/sjydvlp/elefx/svg/icon/outlined/more-horizontal.svg"),
    MORE_VERTICAL("more-vertical", "/com/sjydvlp/elefx/svg/icon/outlined/more-vertical.svg"),

    HOME("home", "/com/sjydvlp/elefx/svg/icon/outlined/home.svg"),
    USER("user", "/com/sjydvlp/elefx/svg/icon/outlined/user.svg"),
    CALENDAR("calendar", "/com/sjydvlp/elefx/svg/icon/outlined/calendar.svg"),
    CLOCK("clock", "/com/sjydvlp/elefx/svg/icon/outlined/clock.svg"),
    EDIT("edit", "/com/sjydvlp/elefx/svg/icon/outlined/edit.svg"),
    DELETE("delete", "/com/sjydvlp/elefx/svg/icon/outlined/delete.svg"),
    COPY("copy", "/com/sjydvlp/elefx/svg/icon/outlined/copy.svg"),
    DOWNLOAD("download", "/com/sjydvlp/elefx/svg/icon/outlined/download.svg"),
    UPLOAD("upload", "/com/sjydvlp/elefx/svg/icon/outlined/upload.svg"),
    REFRESH("refresh", "/com/sjydvlp/elefx/svg/icon/outlined/refresh.svg"),
    FILTER("filter", "/com/sjydvlp/elefx/svg/icon/outlined/filter.svg"),
    SORT("sort", "/com/sjydvlp/elefx/svg/icon/outlined/sort.svg"),
    SHARE("share", "/com/sjydvlp/elefx/svg/icon/outlined/share.svg"),

    INFO("info", "/com/sjydvlp/elefx/svg/icon/outlined/info.svg"),
    INFO_FILLED("info-filled", "/com/sjydvlp/elefx/svg/icon/outlined/info-filled.svg"),
    SUCCESS("success", "/com/sjydvlp/elefx/svg/icon/outlined/success.svg"),
    WARNING("warning", "/com/sjydvlp/elefx/svg/icon/outlined/warning.svg"),
    ERROR("error", "/com/sjydvlp/elefx/svg/icon/outlined/error.svg"),
    QUESTION("question", "/com/sjydvlp/elefx/svg/icon/outlined/question.svg"),

    STAR("star", "/com/sjydvlp/elefx/svg/icon/outlined/star.svg"),
    HEART("heart", "/com/sjydvlp/elefx/svg/icon/outlined/heart.svg"),
    EYE("eye", "/com/sjydvlp/elefx/svg/icon/outlined/eye.svg"),
    LOCK("lock", "/com/sjydvlp/elefx/svg/icon/outlined/lock.svg"),
    UNLOCK("unlock", "/com/sjydvlp/elefx/svg/icon/outlined/unlock.svg"),
    FOLDER("folder", "/com/sjydvlp/elefx/svg/icon/outlined/folder.svg"),
    FILE("file", "/com/sjydvlp/elefx/svg/icon/outlined/file.svg"),
    IMAGE("image", "/com/sjydvlp/elefx/svg/icon/outlined/image.svg"),
    CAMERA("camera", "/com/sjydvlp/elefx/svg/icon/outlined/camera.svg"),
    PLAY("play", "/com/sjydvlp/elefx/svg/icon/outlined/play.svg"),
    PAUSE("pause", "/com/sjydvlp/elefx/svg/icon/outlined/pause.svg"),
    LOCATION("location", "/com/sjydvlp/elefx/svg/icon/outlined/location.svg"),
    PHONE("phone", "/com/sjydvlp/elefx/svg/icon/outlined/phone.svg"),
    MAIL("mail", "/com/sjydvlp/elefx/svg/icon/outlined/mail.svg"),
    BELL("bell", "/com/sjydvlp/elefx/svg/icon/outlined/bell.svg"),
    LINK("link", "/com/sjydvlp/elefx/svg/icon/outlined/link.svg"),
    EXTERNAL_LINK("external-link", "/com/sjydvlp/elefx/svg/icon/outlined/external-link.svg");

    private final String name;

    private final String path;

    EleFXOutlinedIconType(String name, String path) {
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
        return false;
    }
}
