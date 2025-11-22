# 课程节次设置重构需求文档

## Introduction

重构课程设置页面的节次配置逻辑，从单一的"每天节次数"和"时间轴紧凑度"改为按时段（上午/下午/晚上）分别配置节次数量，提供更灵活和符合实际使用场景的配置方式。

## Glossary

- **System**: 课程表应用
- **User**: 使用课程表的学生或教师
- **Period**: 课程节次
- **Time Segment**: 时段（上午/下午/晚上）
- **Template**: 预设的课程配置模板

## Requirements

### Requirement 1: 移除时间轴紧凑度设置

**User Story:** 作为用户，我希望移除不直观的"时间轴紧凑度"设置，以便更简单地配置课程表

#### Acceptance Criteria

1. WHEN User打开课程设置页面，THE System SHALL不显示"时间轴紧凑度"滑块
2. THE System SHALL从CourseSettings数据模型中移除timelineCompactness字段
3. THE System SHALL从所有相关UI组件中移除timelineCompactness的引用

### Requirement 2: 添加分时段节次配置

**User Story:** 作为用户，我希望能够分别设置上午、下午和晚上的节次数量，以便更灵活地配置课程表

#### Acceptance Criteria

1. THE System SHALL在CourseSettings中添加morningPeriods字段（默认值4）
2. THE System SHALL在CourseSettings中添加afternoonPeriods字段（默认值4）
3. THE System SHALL在CourseSettings中添加eveningPeriods字段（默认值0）
4. WHEN User打开课程设置页面，THE System SHALL显示三个独立的节次数量选择器
5. THE System SHALL计算总节次数为morningPeriods + afternoonPeriods + eveningPeriods

### Requirement 3: 更新大学课程模板

**User Story:** 作为大学生用户，我希望大学课程模板符合实际的课程安排，以便快速配置课程表

#### Acceptance Criteria

1. WHEN User选择大学课程模板，THE System SHALL设置morningPeriods为4
2. WHEN User选择大学课程模板，THE System SHALL设置afternoonPeriods为4
3. WHEN User选择大学课程模板，THE System SHALL设置eveningPeriods为1
4. THE System SHALL保持其他模板的现有配置不变

### Requirement 4: 自动生成节次时间

**User Story:** 作为用户，我希望系统根据我设置的节次数量自动生成合理的时间安排，以便无需手动配置每个节次

#### Acceptance Criteria

1. WHEN User修改上午节次数量，THE System SHALL自动生成上午节次的时间表
2. WHEN User修改下午节次数量，THE System SHALL自动生成下午节次的时间表
3. WHEN User修改晚上节次数量，THE System SHALL自动生成晚上节次的时间表
4. THE System SHALL在上午和下午之间插入午休时段
5. THE System SHALL在下午和晚上之间插入晚休时段（如果有晚上节次）
