# elefx

## Layout 栅格布局

参考 [Element Plus Layout](https://element-plus.org/en-US/component/layout)，
通过 `com.sjydvlp.elefx.component.layout` 中的 `EleFXRow` 和 `EleFXCol` 创建 24 栏布局。
组件沿用 `EleFXButton` 的 JavaFX 属性、`Themable` 和 Scene Builder 接入方式。

```java
import com.sjydvlp.elefx.component.button.EleFXButton;
import com.sjydvlp.elefx.component.layout.*;
import com.sjydvlp.elefx.theme.EleFXThemes;
import javafx.scene.layout.VBox;

EleFXRow basic = new EleFXRow(20,
        new EleFXCol(12, new EleFXButton("左侧")),
        new EleFXCol(12, new EleFXButton("右侧")));

EleFXCol sidebar = new EleFXCol(6, new EleFXButton("侧栏"));
EleFXCol main = new EleFXCol(12, new EleFXButton("主内容"));
main.setOffset(6);
EleFXRow offset = new EleFXRow(sidebar, main);

EleFXRow aligned = new EleFXRow(
        new EleFXCol(6, new EleFXButton("A")),
        new EleFXCol(6, new EleFXButton("B")));
aligned.setJustify(EleFXRowJustify.SPACE_BETWEEN);
aligned.setAlign(EleFXRowAlign.MIDDLE);

VBox content = new VBox(20, basic, offset, aligned);
EleFXThemes.DEFAULT.applyOn(content);
// 也可单独使用 EleFXThemes.LAYOUT.applyOn(row)。
```

| 组件 | 属性 | 默认值 / 说明 |
| --- | --- | --- |
| Row | `gutter` | 0；列内容之间的间距，单位为 JavaFX 逻辑像素 |
| Row | `justify` | `START`；另有 `CENTER`、`END`、`SPACE_BETWEEN`、`SPACE_AROUND`、`SPACE_EVENLY` |
| Row | `align` | `STRETCH`；另有 `TOP`、`MIDDLE`、`BOTTOM` |
| Col | `span` | 24；占用的栅格数，0 表示隐藏且不占位 |
| Col | `offset` | 0；左侧空出的栅格数，参与换行计算 |
| Col | `push` / `pull` | 0；向右 / 左移动的栅格数，不改变其他列的位置 |
| Col | `xs` / `sm` / `md` / `lg` / `xl` | 可选的响应式配置 |

`span + offset` 累计超过 24 时自动换行。列内容按 `StackPane` 布局，可使用
`StackPane.setAlignment`、`StackPane.setMargin` 和列本身的 padding。
`gutter` 在每列左右各增加一半内侧留白，保留已有 padding；行宽包含两端各半个 gutter，
不使用网页 CSS 的负外边距。行间距可通过外层 `VBox` 设置。
普通 Node 直接放入 Row 时占满 24 栏，`managed=false` 的节点不参与布局。
布局由 JavaFX 节点实现，因此没有 HTML `tag` 属性。

### 响应式布局

```java
EleFXCol col = new EleFXCol(12, new EleFXButton("响应式内容"));
col.setXs(24);
col.setSm(12);
col.setMd(new EleFXColSize(8, 4, null, null)); // span、offset、push、pull
col.setLg(6);
col.setXl(4);
EleFXRow responsive = new EleFXRow(16, col);
```

断点依据 `Scene` 内容宽度，未加入场景时使用 Row 宽度：`xs < 768`、`sm ≥ 768`、
`md ≥ 992`、`lg ≥ 1200`、`xl ≥ 1920`。`xs` 只在小于 768 时生效；其余断点从小到大
叠加，配置中为 `null` 的字段继承前一有效配置或基础属性。设置 `setMd(null)` 等可清除配置。

Col 支持 `hidden-xs-only`、`hidden-xl-only`，以及 `sm`、`md`、`lg` 的
`hidden-*-only`、`hidden-*-and-down`、`hidden-*-and-up` 样式类，例如：

```java
col.getStyleClass().add("hidden-sm-and-down");
```

隐藏时保留 `managed`，临时设置未绑定的 `visible=false`，恢复时还原原值。
若 `visible` 已绑定，则使用未绑定的 `clip` 进行空裁剪，并临时设置未绑定的 `disable=true`
以退出键盘交互；恢复时还原原状态。调用方的属性绑定始终保留；若相关属性均已绑定，
列仍不占位，但内容显隐和交互由调用方控制。
栅格数 setter 接受 0–24，`gutter` setter 接受非负有限数；直接绑定的非法栅格值在布局时
限制到 0–24，非法间距按 0 处理，不修改绑定源。

### FXML

```xml
<?import com.sjydvlp.elefx.component.layout.*?>
<?import com.sjydvlp.elefx.component.button.EleFXButton?>
<EleFXRow xmlns:fx="http://javafx.com/fxml/1" gutter="20" justify="SPACE_BETWEEN">
    <EleFXCol span="12" xs="24" sm="12">
        <EleFXButton text="左侧" />
    </EleFXCol>
    <EleFXCol span="12" xs="24">
        <md><EleFXColSize span="8" offset="4" /></md>
        <EleFXButton text="右侧" />
    </EleFXCol>
</EleFXRow>
```

运行布局测试：`mvn test -Dformatter.skip=true`。
