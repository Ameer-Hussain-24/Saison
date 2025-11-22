package takagi.ru.saison.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 网格布局配置数据模型
 * 定义课程表网格布局的尺寸和间距配置（紧凑布局）
 * 
 * @property cellHeight 每个单元格的高度
 * @property periodColumnWidth 节次列宽度
 * @property timeColumnWidth 时间列宽度（已废弃，使用periodColumnWidth）
 * @property dayColumnMinWidth 每天列的最小宽度
 * @property headerHeight 标题行高度
 * @property horizontalPadding 水平内边距
 * @property verticalPadding 垂直内边距
 * @property cardCornerRadius 课程卡片圆角
 * @property cellCornerRadius 单元格圆角
 * @property cardElevation 课程卡片阴影
 * @property spacing 间距
 * @property showBreakSeparators 是否显示休息时段分隔行
 */
data class GridLayoutConfig(
    val cellHeight: Dp = 70.dp,
    val periodColumnWidth: Dp = 60.dp,
    val timeColumnWidth: Dp = 80.dp,  // 已废弃
    val dayColumnMinWidth: Dp = 100.dp,
    val headerHeight: Dp = 56.dp,
    val horizontalPadding: Dp = 4.dp,
    val verticalPadding: Dp = 2.dp,
    val cardCornerRadius: Dp = 10.dp,
    val cellCornerRadius: Dp = 4.dp,
    val cardElevation: Dp = 1.dp,
    val spacing: Dp = 2.dp,
    val showBreakSeparators: Boolean = true
)
