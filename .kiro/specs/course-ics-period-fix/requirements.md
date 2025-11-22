# Requirements Document

## Introduction

修复ICS导入后课程全部显示在第1节的问题。当前问题是导入的课程的 `periodStart` 和 `periodEnd` 字段都是 `null`，导致课程无法正确显示在对应的节次位置。

## Glossary

- **ICS文件**: iCalendar格式的课程表文件
- **节次 (Period)**: 课程表中的时间段，如第1节、第2节等
- **PeriodMatcher**: 负责将课程时间匹配到节次的工具类
- **CourseConverter**: 负责将解析后的ICS数据转换为Course对象的转换器

## Requirements

### Requirement 1: 正确提取节次信息

**User Story:** 作为用户，我希望从ICS文件导入的课程能够正确显示在对应的节次位置，这样我就能清楚地看到每节课的安排

#### Acceptance Criteria

1. WHEN ICS文件的DESCRIPTION字段包含"第X-Y节"格式的节次信息，THE System SHALL 正确提取开始节次X和结束节次Y
2. WHEN ICS文件的DESCRIPTION字段包含"第X节"格式的节次信息，THE System SHALL 正确提取节次X并将其同时作为开始和结束节次
3. WHEN 节次信息提取成功，THE System SHALL 将提取的节次号赋值给Course对象的periodStart和periodEnd字段
4. WHEN 节次信息提取失败，THE System SHALL 使用时间匹配算法尝试匹配节次
5. WHEN 时间匹配也失败，THE System SHALL 将periodStart和periodEnd设置为null，并将isCustomTime设置为true

### Requirement 2: 调试和日志

**User Story:** 作为开发者，我需要详细的日志来诊断节次匹配问题，这样我就能快速定位和修复bug

#### Acceptance Criteria

1. WHEN PeriodMatcher尝试匹配节次，THE System SHALL 记录输入的时间、描述和可用节次列表
2. WHEN 从描述中提取节次信息，THE System SHALL 记录提取结果（成功或失败）
3. WHEN 使用时间匹配算法，THE System SHALL 记录匹配过程和结果
4. WHEN 课程转换完成，THE System SHALL 记录每个课程的periodStart、periodEnd和isCustomTime值

### Requirement 3: 自动学期开始日期检测

**User Story:** 作为用户，我希望导入ICS文件时系统能够自动检测并设置学期开始日期，这样我就不需要手动配置就能看到正确的周次

#### Acceptance Criteria

1. WHEN 导入ICS文件，THE System SHALL 从所有课程中找到最早的开始日期
2. WHEN 找到最早的开始日期，THE System SHALL 计算该日期所在周的周一作为学期开始日期
3. WHEN 学期开始日期已设置，THE System SHALL 根据学期开始日期和当前日期计算当前周次
4. WHEN 创建或更新学期，THE System SHALL 使用检测到的学期开始日期
5. WHEN 学期开始日期设置成功，THE System SHALL 在日志中记录学期开始日期和当前周次

### Requirement 4: 周次显示和滑动边界修复

**User Story:** 作为用户，我希望课程表顶部能够正确显示当前周次，并且左右滑动时不能超出学期周次范围，这样我就能清楚地知道当前是第几周

#### Acceptance Criteria

1. WHEN 学期开始日期已正确设置，THE System SHALL 根据当前日期计算并显示正确的当前周次
2. WHEN 用户左右滑动课程表，THE System SHALL 更新顶部显示的周次（如"第11/18周"）
3. WHEN 用户滑动到第1周，THE System SHALL 禁止继续向左滑动
4. WHEN 用户滑动到最后一周（如第18周），THE System SHALL 禁止继续向右滑动
5. WHEN 用户不在当前周，THE System SHALL 显示"回到当前周"按钮
6. WHEN 用户点击"回到当前周"按钮，THE System SHALL 滑动到当前周并更新显示

### Requirement 5: 显示修复

**User Story:** 作为用户，我希望导入的课程能够正确显示在课程表网格中，这样我就能直观地查看课程安排

#### Acceptance Criteria

1. WHEN Course对象的periodStart和periodEnd都不为null，THE System SHALL 在网格中将课程显示在对应的节次位置
2. WHEN Course对象的periodStart或periodEnd为null，THE System SHALL 根据startTime和endTime计算课程应该显示的位置
3. WHEN 无法确定课程位置，THE System SHALL 在网格中显示警告或将课程标记为自定义时间课程
