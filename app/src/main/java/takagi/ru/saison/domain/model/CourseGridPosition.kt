package takagi.ru.saison.domain.model

import androidx.compose.ui.unit.Dp

/**
 * 课程网格位置数据模型
 * 用于在网格布局中定位课程卡片
 * 
 * @property offsetY 距离顶部的偏移量
 * @property height 卡片高度
 */
data class CourseGridPosition(
    val offsetY: Dp,
    val height: Dp
)
