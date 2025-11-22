# 课程表网格布局重构 - 需求文档

## 简介

完全重构课程表界面，实现与参考UI一致的网格布局，包括周次选择、时间分段显示、以及可编辑的课程卡片。

## 术语表

- **System**: Saison课程表系统
- **User**: 使用课程表功能的学生用户
- **Week Selector**: 周次选择器，显示当前周次并允许切换
- **Grid Timetable**: 网格式课程表，以表格形式展示课程
- **Period**: 课时节次（如第1节、第2节）
- **Time Slot**: 时间段（如08:30-09:15）
- **Break Section**: 休息时段分隔区域（午休、晚修）
- **Course Card**: 课程卡片，显示在网格中的课程信息
- **Bottom Drawer**: 底部抽屉，用于选择周次的弹出界面

## 需求

### 需求 1: 周次选择器

**用户故事:** 作为学生用户，我想要在课程表顶部看到当前周次并能快速切换，以便查看不同周的课程安排

#### 验收标准

1. WHEN System显示课程表界面时，THE System SHALL在左上角显示当前周次按钮（格式："X周"）
2. WHEN User点击周次按钮时，THE System SHALL从底部弹出周次选择抽屉
3. WHEN 周次选择抽屉显示时，THE System SHALL以阿拉伯数字列表形式显示所有可用周次
4. WHEN User在抽屉中选择某个周次时，THE System SHALL更新课程表显示该周的课程
5. WHEN User选择周次后，THE System SHALL自动关闭抽屉并更新周次按钮文本

### 需求 2: 日期头部显示

**用户故事:** 作为学生用户，我想要在课程表顶部看到周一到周日的日期，以便了解每天的具体日期

#### 验收标准

1. WHEN System显示课程表时，THE System SHALL在顶部显示一行包含周一到周日的日期头部
2. WHEN 显示日期头部时，THE System SHALL为每一天显示星期几和具体日期（格式："一\n11/3"）
3. WHEN 当前日期在显示的周内时，THE System SHALL高亮显示当天的日期列
4. WHEN User切换周次时，THE System SHALL更新日期头部显示对应周的日期

### 需求 3: 网格时间轴

**用户故事:** 作为学生用户，我想要在课程表左侧看到清晰的节次和时间信息，以便了解每节课的时间安排

#### 验收标准

1. WHEN System显示课程表时，THE System SHALL在最左侧列显示所有课时节次
2. WHEN 显示节次时，THE System SHALL为每个节次显示节次号和对应的时间段（格式："1\n08:30\n09:15"）
3. THE System SHALL确保时间轴列不可编辑
4. WHEN 显示时间轴时，THE System SHALL使用固定宽度以保持布局稳定

### 需求 4: 休息时段分隔

**用户故事:** 作为学生用户，我想要在课程表中看到午休和晚修的分隔标识，以便清楚地区分不同时段

#### 验收标准

1. WHEN System显示课程表时，THE System SHALL在午休时段位置显示"午休"分隔行
2. WHEN System显示课程表时，THE System SHALL在晚修时段位置显示"晚修"分隔行
3. WHEN 显示分隔行时，THE System SHALL使分隔行横跨所有日期列
4. WHEN 显示分隔行时，THE System SHALL使用与课程卡片不同的视觉样式以示区分

### 需求 5: 网格课程卡片

**用户故事:** 作为学生用户，我想要在网格中看到课程卡片并能点击编辑，以便管理我的课程信息

#### 验收标准

1. WHEN System显示课程表时，THE System SHALL在对应的日期和节次位置显示课程卡片
2. WHEN 显示课程卡片时，THE System SHALL显示课程名称、地点等基本信息
3. WHEN User点击课程卡片时，THE System SHALL打开课程编辑界面
4. WHEN 某个时间格子没有课程时，THE System SHALL显示空白可点击区域
5. WHEN User点击空白区域时，THE System SHALL打开添加课程界面并预填充对应的时间和日期

### 需求 6: 网格布局响应

**用户故事:** 作为学生用户，我想要课程表能够流畅滚动并保持头部固定，以便在查看长课表时保持方向感

#### 验收标准

1. WHEN 课程表内容超出屏幕高度时，THE System SHALL允许垂直滚动
2. WHEN User滚动课程表时，THE System SHALL保持日期头部固定在顶部
3. WHEN User滚动课程表时，THE System SHALL保持时间轴列固定在左侧
4. WHEN User滚动课程表时，THE System SHALL保持周次选择器固定在左上角
5. THE System SHALL确保网格单元格对齐且大小一致

### 需求 7: 主题适配

**用户故事:** 作为学生用户，我想要课程表界面遵循Material Design 3规范并适配当前主题，以便获得一致的视觉体验

#### 验收标准

1. THE System SHALL使用Material Design 3的颜色系统为课程卡片着色
2. WHEN 系统主题改变时，THE System SHALL更新课程表的颜色方案
3. THE System SHALL为网格线使用适当的分隔线颜色
4. THE System SHALL为分隔行使用表面容器颜色
5. THE System SHALL确保所有文本颜色符合可访问性对比度要求
