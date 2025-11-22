# Requirements Document

## Introduction

修复ICS课程导入时的时间匹配问题。当前所有导入的课程都被错误地分配到第1节课，需要实现智能时间匹配功能，并提供用户选择使用当前课表时间或根据导入文件自动匹配时间的选项。

## Glossary

- **System**: Saison课程管理系统
- **ICS File**: iCalendar格式的课程表文件
- **Period**: 课程节次，包含节次编号、开始时间和结束时间
- **Time Matching**: 将ICS文件中的时间映射到课表节次的过程
- **Import Dialog**: 导入时显示的选项对话框

## Requirements

### Requirement 1: 时间匹配选项对话框

**User Story:** 作为用户，我希望在导入ICS文件时能够选择时间匹配方式，以便灵活处理不同格式的课表文件

#### Acceptance Criteria

1. WHEN THE System开始导入ICS文件, THE System SHALL显示时间匹配选项对话框
2. THE System SHALL提供"使用当前课表时间"选项
3. THE System SHALL提供"根据导入文件自动匹配时间"选项
4. WHEN用户选择"使用当前课表时间", THE System SHALL使用现有的节次时间配置
5. WHEN用户选择"根据导入文件自动匹配时间", THE System SHALL根据ICS文件中的时间自动创建或匹配节次

### Requirement 2: 智能时间匹配算法

**User Story:** 作为用户，我希望系统能够智能地将ICS文件中的时间匹配到正确的节次，以便导入的课程显示在正确的时间段

#### Acceptance Criteria

1. WHEN ICS文件包含节次信息（如"第7-8节"）, THE System SHALL优先使用节次信息进行匹配
2. WHEN ICS文件不包含节次信息, THE System SHALL根据开始时间和结束时间匹配最接近的节次
3. WHEN找不到匹配的节次, THE System SHALL创建自定义时间的课程
4. THE System SHALL正确解析时间格式（如15:30-17:00）
5. THE System SHALL支持跨节次的课程（如第7-8节）

### Requirement 3: 节次时间自动创建

**User Story:** 作为用户，我希望系统能够根据导入文件自动创建节次时间配置，以便快速设置新学期的课表

#### Acceptance Criteria

1. WHEN用户选择"根据导入文件自动匹配时间", THE System SHALL分析ICS文件中的所有时间段
2. THE System SHALL识别不同的时间段并创建对应的节次配置
3. THE System SHALL按时间顺序为节次分配编号
4. THE System SHALL避免创建重复的节次配置
5. THE System SHALL在导入完成后保存新的节次配置

### Requirement 4: 错误处理和用户反馈

**User Story:** 作为用户，我希望在时间匹配过程中遇到问题时能够得到清晰的提示，以便了解问题并采取相应措施

#### Acceptance Criteria

1. WHEN时间匹配失败, THE System SHALL显示具体的错误信息
2. WHEN ICS文件包含无效的时间格式, THE System SHALL提示用户并跳过该课程
3. WHEN创建的节次与现有节次冲突, THE System SHALL提示用户选择处理方式
4. THE System SHALL在导入预览界面显示每门课程的匹配结果
5. THE System SHALL允许用户在导入前手动调整节次匹配
