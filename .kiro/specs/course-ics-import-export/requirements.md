# 课程表导入导出功能需求文档

## 简介

本功能允许用户从ICS（iCalendar）文件导入课程表数据，并将现有课程表导出为ICS格式文件。这使得用户可以方便地从其他课程表应用（如WakeUpSchedule）迁移数据，或将课程表分享到其他日历应用。

## 术语表

- **ICS文件**: iCalendar格式的日历文件，使用VEVENT组件存储事件信息
- **VEVENT**: ICS文件中的事件组件，包含课程的时间、地点、重复规则等信息
- **RRULE**: 重复规则，定义事件的重复模式（如每周重复）
- **CourseImporter**: 负责解析ICS文件并转换为Course对象的系统组件
- **CourseExporter**: 负责将Course对象转换为ICS格式的系统组件
- **SaisonApp**: 本应用系统

## 需求

### 需求 1: ICS文件导入

**用户故事:** 作为用户，我希望能够导入ICS格式的课程表文件，以便快速将其他应用的课程数据迁移到本应用。

#### 验收标准

1. WHEN 用户选择导入ICS文件，THE SaisonApp SHALL 显示文件选择器界面
2. WHEN 用户选择有效的ICS文件，THE SaisonApp SHALL 解析文件中的所有VEVENT组件
3. WHEN ICS文件包含课程事件，THE SaisonApp SHALL 提取课程名称、地点、时间、重复规则等信息
4. WHEN 解析完成，THE SaisonApp SHALL 显示导入预览界面，展示将要导入的课程列表
5. WHEN 用户确认导入，THE SaisonApp SHALL 将所有课程保存到当前选中的学期
6. IF ICS文件格式无效或解析失败，THEN THE SaisonApp SHALL 显示错误提示信息

### 需求 2: 课程数据映射

**用户故事:** 作为用户，我希望导入的课程数据能够正确映射到应用的数据模型，以便课程信息完整准确。

#### 验收标准

1. WHEN 解析VEVENT的SUMMARY字段，THE CourseImporter SHALL 将其映射为课程名称
2. WHEN 解析VEVENT的LOCATION字段，THE CourseImporter SHALL 将其映射为课程地点
3. WHEN 解析VEVENT的DTSTART和DTEND字段，THE CourseImporter SHALL 提取开始时间和结束时间
4. WHEN 解析VEVENT的RRULE字段，THE CourseImporter SHALL 识别重复模式并映射为WeekPattern
5. WHEN RRULE包含UNTIL日期，THE CourseImporter SHALL 将其设置为课程结束日期
6. WHEN DESCRIPTION包含节次信息（如"第7-8节"），THE CourseImporter SHALL 提取并设置periodStart和periodEnd
7. WHEN VALARM存在，THE CourseImporter SHALL 提取提醒时间并设置notificationMinutes

### 需求 3: 重复规则处理

**用户故事:** 作为用户，我希望导入的课程能够正确识别重复模式，以便课程按正确的周次显示。

#### 验收标准

1. WHEN RRULE为"FREQ=WEEKLY;INTERVAL=1"且无其他限制，THE CourseImporter SHALL 设置WeekPattern为ALL
2. WHEN 多个VEVENT具有相同课程名称但不同日期范围，THE CourseImporter SHALL 识别为自定义周次模式
3. WHEN 识别为自定义周次，THE CourseImporter SHALL 计算周数列表并设置customWeeks
4. WHEN RRULE包含BYDAY参数，THE CourseImporter SHALL 提取星期几信息并设置dayOfWeek
5. IF RRULE格式不支持，THEN THE CourseImporter SHALL 使用默认的ALL模式

### 需求 4: 课程表导出

**用户故事:** 作为用户，我希望能够将当前学期的课程表导出为ICS文件，以便在其他日历应用中查看或备份。

#### 验收标准

1. WHEN 用户选择导出课程表，THE SaisonApp SHALL 显示学期选择界面
2. WHEN 用户选择要导出的学期，THE CourseExporter SHALL 获取该学期的所有课程
3. WHEN 生成ICS文件，THE CourseExporter SHALL 为每个课程创建VEVENT组件
4. WHEN 课程有重复模式，THE CourseExporter SHALL 生成相应的RRULE规则
5. WHEN 课程为自定义周次，THE CourseExporter SHALL 生成多个VEVENT以表示不连续的周次
6. WHEN ICS文件生成完成，THE SaisonApp SHALL 提供文件保存或分享选项
7. THE CourseExporter SHALL 包含VTIMEZONE组件以确保时区信息正确

### 需求 5: 导入预览与确认

**用户故事:** 作为用户，我希望在正式导入前能够预览课程列表，以便确认数据正确性并选择性导入。

#### 验收标准

1. WHEN 解析完成，THE SaisonApp SHALL 显示课程预览列表，包含课程名称、时间、地点
2. THE SaisonApp SHALL 为每个课程提供复选框，允许用户选择要导入的课程
3. THE SaisonApp SHALL 显示将要导入的课程总数
4. WHEN 用户取消选择某些课程，THE SaisonApp SHALL 仅导入选中的课程
5. THE SaisonApp SHALL 提供"全选"和"取消全选"功能
6. WHEN 检测到重复课程（相同名称和时间），THE SaisonApp SHALL 显示警告提示

### 需求 6: 颜色分配

**用户故事:** 作为用户，我希望导入的课程能够自动分配不同的颜色，以便在课程表中区分不同课程。

#### 验收标准

1. WHEN 导入课程，THE CourseImporter SHALL 使用CourseColorAssigner为每个课程分配颜色
2. THE CourseImporter SHALL 确保相同名称的课程使用相同颜色
3. THE CourseImporter SHALL 确保不同课程使用不同颜色以提高可区分性
4. WHEN 导出课程，THE CourseExporter SHALL 在DESCRIPTION中包含颜色信息（可选）

### 需求 7: 错误处理与用户反馈

**用户故事:** 作为用户，我希望在导入或导出过程中遇到错误时能够得到清晰的提示，以便了解问题并采取相应措施。

#### 验收标准

1. IF 选择的文件不是有效的ICS格式，THEN THE SaisonApp SHALL 显示"文件格式无效"错误消息
2. IF ICS文件为空或不包含VEVENT，THEN THE SaisonApp SHALL 显示"未找到课程数据"提示
3. IF 解析过程中发生异常，THEN THE SaisonApp SHALL 记录错误日志并显示通用错误消息
4. WHEN 导入成功，THE SaisonApp SHALL 显示成功消息，包含导入的课程数量
5. WHEN 导出成功，THE SaisonApp SHALL 显示成功消息并提供文件位置信息
6. IF 导出过程中发生IO错误，THEN THE SaisonApp SHALL 显示"文件保存失败"错误消息

### 需求 8: 性能要求

**用户故事:** 作为用户，我希望导入和导出操作能够快速完成，以便获得流畅的使用体验。

#### 验收标准

1. WHEN 导入包含100个以内的课程事件，THE SaisonApp SHALL 在3秒内完成解析和预览显示
2. WHEN 导出包含50个以内的课程，THE SaisonApp SHALL 在2秒内完成ICS文件生成
3. THE SaisonApp SHALL 在后台线程执行导入导出操作，避免阻塞UI线程
4. WHILE 执行导入导出操作，THE SaisonApp SHALL 显示进度指示器
