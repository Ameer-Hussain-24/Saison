# 课程表单双周显示修复 - 需求文档

## 简介

课程表页面在显示单双周课程时存在问题，导致显示了本周不应该显示的课程。需要重构课程表显示区域的UI和过滤逻辑，确保单双周课程能够正确显示，只显示当前周应该上的课程。

## 术语表

- **System**: 课程表显示系统
- **Course**: 课程实体，包含名称、时间、地点、周模式等信息
- **WeekPattern**: 周模式，包括全部周(ALL)、单周(ODD)、双周(EVEN)、A周(A)、B周(B)、自定义周(CUSTOM)
- **CurrentWeek**: 当前周数，基于学期开始日期计算得出
- **WeekOffset**: 周数偏移量，用于查看其他周的课程表
- **ContinuousTimelineView**: 连续时间轴视图组件，用于显示课程表
- **CourseViewModel**: 课程视图模型，负责课程数据的管理和过滤

## 需求

### 需求 1: 周数过滤准确性

**用户故事:** 作为学生用户，我希望课程表只显示当前周应该上的课程，这样我就能准确了解本周的课程安排

#### 验收标准

1. WHEN THE System 计算当前周数时，THE System SHALL 基于学期开始日期和当前日期准确计算周数
2. WHEN 用户查看课程表时，THE System SHALL 根据当前周数过滤课程列表
3. WHEN 课程的周模式为单周(ODD)时，THE System SHALL 仅在奇数周显示该课程
4. WHEN 课程的周模式为双周(EVEN)时，THE System SHALL 仅在偶数周显示该课程
5. WHEN 课程的周模式为全部周(ALL)时，THE System SHALL 在所有周显示该课程

### 需求 2: 自定义周模式支持

**用户故事:** 作为学生用户，我希望系统能正确处理自定义周模式的课程，这样我就能准确安排特殊周次的课程

#### 验收标准

1. WHEN 课程的周模式为自定义(CUSTOM)时，THE System SHALL 检查当前周数是否在自定义周数列表中
2. WHEN 当前周数在自定义周数列表中时，THE System SHALL 显示该课程
3. WHEN 当前周数不在自定义周数列表中时，THE System SHALL 隐藏该课程

### 需求 3: 周数切换功能

**用户故事:** 作为学生用户，我希望能够查看其他周的课程安排，这样我就能提前规划或回顾课程

#### 验收标准

1. WHEN 用户左右滑动课程表时，THE System SHALL 更新周数偏移量
2. WHEN 周数偏移量改变时，THE System SHALL 重新计算显示周数
3. WHEN 显示周数改变时，THE System SHALL 根据新的周数重新过滤课程
4. WHEN 用户点击"回到当前周"按钮时，THE System SHALL 将周数偏移量重置为0

### 需求 4: 课程显示一致性

**用户故事:** 作为学生用户，我希望课程表显示的课程与实际周数一致，这样我就不会混淆课程安排

#### 验收标准

1. THE System SHALL 在HorizontalPager的每一页中根据对应周数过滤课程
2. WHEN 用户切换到某一周时，THE System SHALL 仅显示该周应该上的课程
3. WHEN 某一周没有课程时，THE System SHALL 显示空状态提示
4. THE System SHALL 在顶部栏显示当前查看的周数

### 需求 5: 过滤逻辑优化

**用户故事:** 作为学生用户，我希望课程过滤逻辑清晰准确，这样系统就能正确判断哪些课程应该显示

#### 验收标准

1. THE System SHALL 在CourseViewModel中提供统一的课程周数判断方法
2. THE System SHALL 在CourseScreen中使用该方法过滤每一页的课程
3. THE System SHALL 确保过滤后的课程列表不包含本周不应显示的课程
4. THE System SHALL 在ContinuousTimelineView中接收已过滤的课程列表
5. THE System SHALL 移除ContinuousTimelineView中的重复过滤逻辑

### 需求 6: 智能紧凑显示

**用户故事:** 作为学生用户，我希望课程表能够智能地紧凑显示，这样我就能在一屏内看到更多课程信息，提高浏览效率

#### 验收标准

1. WHEN 某个时间段在一周内所有天都没有课程时，THE System SHALL 自动压缩该时间段的显示高度
2. WHEN 某个时间段至少有一天有课程时，THE System SHALL 按正常高度显示该时间段
3. THE System SHALL 计算每个时间段（小时）在一周内的课程分布情况
4. THE System SHALL 根据课程分布动态调整时间段的显示高度
5. WHEN 压缩空白时间段时，THE System SHALL 保持最小可识别高度以显示时间标签

### 需求 7: 时间轴优化

**用户故事:** 作为学生用户，我希望时间轴显示更加合理，这样我就能快速定位课程时间

#### 验收标准

1. THE System SHALL 根据实际课程时间动态计算时间轴的起始和结束时间
2. THE System SHALL 仅显示有课程的时间范围，避免显示大量空白时间
3. WHEN 课程开始时间早于设置的第一节课时间时，THE System SHALL 自动扩展时间轴起始时间
4. WHEN 课程结束时间晚于常规时间时，THE System SHALL 自动扩展时间轴结束时间
5. THE System SHALL 在时间轴上清晰标注每个小时的时间点

### 需求 8: 视觉优化

**用户故事:** 作为学生用户，我希望课程表的视觉呈现更加清晰美观，这样我就能更愉快地使用应用

#### 验收标准

1. THE System SHALL 为课程卡片提供清晰的视觉层次
2. THE System SHALL 在课程卡片上显示课程名称、时间、地点、教师等关键信息
3. WHEN 课程卡片空间有限时，THE System SHALL 优先显示最重要的信息
4. THE System SHALL 使用合适的字体大小和颜色对比度确保可读性
5. THE System SHALL 在课程卡片上显示周模式标识（单周/双周等）
