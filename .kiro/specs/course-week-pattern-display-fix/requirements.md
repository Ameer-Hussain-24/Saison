# 课程单双周显示修复 - 需求文档

## 简介

用户报告课程表中的单双周课程没有正确分开显示。需要诊断并修复周数过滤逻辑，确保单周课程只在单周显示，双周课程只在双周显示。

## 术语表

- **System**: 课程表显示系统
- **Course**: 课程实体
- **WeekPattern**: 周模式枚举，包括 ALL（全周）、ODD（单周）、EVEN（双周）、CUSTOM（自定义周）、A周、B周
- **CurrentWeek**: 当前周数
- **isCourseActiveInWeek**: 判断课程在某周是否上课的方法

## 需求

### 需求 1: 单周课程过滤

**用户故事:** 作为学生用户，我希望单周课程只在奇数周显示，这样我就能准确了解本周的课程安排

#### 验收标准

1. WHEN 课程的 weekPattern 为 ODD 时，THE System SHALL 仅在奇数周（1, 3, 5, 7...）显示该课程
2. WHEN 当前周为偶数周时，THE System SHALL 不显示 weekPattern 为 ODD 的课程
3. THE System SHALL 使用 `week % 2 == 1` 判断是否为单周

### 需求 2: 双周课程过滤

**用户故事:** 作为学生用户，我希望双周课程只在偶数周显示，这样我就能准确了解本周的课程安排

#### 验收标准

1. WHEN 课程的 weekPattern 为 EVEN 时，THE System SHALL 仅在偶数周（2, 4, 6, 8...）显示该课程
2. WHEN 当前周为奇数周时，THE System SHALL 不显示 weekPattern 为 EVEN 的课程
3. THE System SHALL 使用 `week % 2 == 0` 判断是否为双周

### 需求 3: 全周课程显示

**用户故事:** 作为学生用户，我希望全周课程在所有周都显示

#### 验收标准

1. WHEN 课程的 weekPattern 为 ALL 时，THE System SHALL 在所有周显示该课程
2. THE System SHALL 不对 ALL 模式的课程进行周数过滤

### 需求 4: 自定义周课程过滤

**用户故事:** 作为学生用户，我希望自定义周课程只在指定的周数显示

#### 验收标准

1. WHEN 课程的 weekPattern 为 CUSTOM 时，THE System SHALL 检查 customWeeks 列表
2. WHEN 当前周在 customWeeks 列表中时，THE System SHALL 显示该课程
3. WHEN 当前周不在 customWeeks 列表中时，THE System SHALL 不显示该课程
4. WHEN customWeeks 为 null 或空时，THE System SHALL 不显示该课程

### 需求 5: 周模式标识显示

**用户故事:** 作为学生用户，我希望在课程卡片上看到周模式标识，这样我就能快速识别课程的周模式

#### 验收标准

1. WHEN 课程的 weekPattern 不为 ALL 时，THE System SHALL 在课程卡片上显示周模式标识
2. THE System SHALL 为 ODD 模式显示"单周"标识
3. THE System SHALL 为 EVEN 模式显示"双周"标识
4. THE System SHALL 为 A 模式显示"A周"标识
5. THE System SHALL 为 B 模式显示"B周"标识

### 需求 6: 调试和验证

**用户故事:** 作为开发者，我希望能够验证周数过滤逻辑是否正确工作

#### 验收标准

1. THE System SHALL 提供日志输出显示当前周数
2. THE System SHALL 提供日志输出显示过滤前后的课程数量
3. THE System SHALL 提供日志输出显示每个课程的周模式和是否被过滤
