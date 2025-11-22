# 多学期课表管理需求文档

## 简介

本功能旨在支持用户创建和管理多个学期的课表配置，允许用户在不同学期之间切换，方便查看往年课表或提前规划未来学期的课程安排。每个学期配置包含独立的课程数据和学期设置。

## 术语表

- **Semester**: 学期，代表一个完整的教学周期（如"2024秋季学期"、"2025春季学期"）
- **SemesterProfile**: 学期配置，包含学期名称、开始日期、总周数等信息
- **ActiveSemester**: 当前激活的学期，用户正在查看的学期
- **SemesterSwitch**: 学期切换，在不同学期配置之间切换的操作
- **SemesterArchive**: 学期归档，将过去的学期标记为归档状态

## 需求

### 需求 1：学期配置数据模型

**用户故事：** 作为开发者，我需要扩展数据模型以支持多学期管理，以便系统能够存储和管理多个学期的课表数据。

#### 验收标准

1. THE System SHALL 创建 Semester 数据模型，包含学期ID、名称、开始日期、结束日期、总周数、是否归档等字段
2. THE System SHALL 扩展 Course 数据模型，添加 semesterId 外键字段关联到所属学期
3. THE System SHALL 在数据库中创建 semester 表存储学期配置
4. THE System SHALL 提供数据库迁移脚本，为现有课程数据创建默认学期
5. THE System SHALL 确保每个课程必须关联到一个学期

### 需求 2：学期列表管理

**用户故事：** 作为学生用户，我希望能够查看所有学期配置列表，以便了解我创建了哪些学期的课表。

#### 验收标准

1. THE System SHALL 在课程设置中显示学期列表界面
2. THE System SHALL 显示每个学期的名称、时间范围、课程数量
3. THE System SHALL 标识当前激活的学期
4. THE System SHALL 按时间倒序排列学期（最新的在前）
5. THE System SHALL 区分显示活跃学期和归档学期

### 需求 3：创建新学期

**用户故事：** 作为学生用户，我希望能够创建新的学期配置，以便为新学期规划课表。

#### 验收标准

1. THE System SHALL 提供"创建学期"按钮
2. WHEN 用户点击创建学期，THE System SHALL 显示学期创建对话框
3. THE System SHALL 要求用户输入学期名称（如"2025春季学期"）
4. THE System SHALL 要求用户选择学期开始日期
5. THE System SHALL 要求用户设置学期总周数（16-24周）
6. THE System SHALL 验证学期名称不能为空且不能重复
7. WHEN 用户保存新学期，THE System SHALL 创建学期配置并切换到新学期

### 需求 4：学期切换

**用户故事：** 作为学生用户，我希望能够在不同学期之间快速切换，以便查看不同学期的课表。

#### 验收标准

1. THE System SHALL 在课程表顶部显示当前学期名称
2. WHEN 用户点击学期名称，THE System SHALL 显示学期选择器
3. THE System SHALL 在学期选择器中列出所有学期
4. WHEN 用户选择不同学期，THE System SHALL 切换到该学期并刷新课表
5. THE System SHALL 保存用户最后选择的学期，下次打开应用时自动加载

### 需求 5：编辑学期配置

**用户故事：** 作为学生用户，我希望能够编辑学期配置信息，以便修正错误或调整学期设置。

#### 验收标准

1. THE System SHALL 在学期列表中提供编辑按钮
2. WHEN 用户点击编辑，THE System SHALL 显示学期编辑对话框
3. THE System SHALL 允许用户修改学期名称
4. THE System SHALL 允许用户修改学期开始日期
5. THE System SHALL 允许用户修改学期总周数
6. THE System SHALL 验证修改后的数据有效性

### 需求 6：删除学期

**用户故事：** 作为学生用户，我希望能够删除不需要的学期配置，以便保持学期列表整洁。

#### 验收标准

1. THE System SHALL 在学期列表中提供删除按钮
2. WHEN 用户点击删除，THE System SHALL 显示确认对话框
3. THE System SHALL 警告用户删除学期将同时删除该学期的所有课程
4. THE System SHALL 显示该学期的课程数量
5. WHEN 用户确认删除，THE System SHALL 删除学期及其所有关联课程
6. THE System SHALL 禁止删除当前激活的学期（需先切换到其他学期）

### 需求 7：学期归档

**用户故事：** 作为学生用户，我希望能够归档过去的学期，以便区分当前学期和历史学期。

#### 验收标准

1. THE System SHALL 提供归档学期功能
2. WHEN 用户归档学期，THE System SHALL 将学期标记为归档状态
3. THE System SHALL 在学期列表中区分显示归档学期（使用不同颜色或图标）
4. THE System SHALL 允许用户取消归档，恢复学期为活跃状态
5. THE System SHALL 在学期选择器中默认只显示活跃学期，提供选项显示归档学期

### 需求 8：课程关联学期

**用户故事：** 作为学生用户，我希望添加的课程自动关联到当前学期，以便课程数据正确归属。

#### 验收标准

1. WHEN 用户添加新课程，THE System SHALL 自动将课程关联到当前激活的学期
2. THE System SHALL 在课程详情中显示所属学期信息
3. THE System SHALL 允许用户在编辑课程时更改所属学期
4. THE System SHALL 在课程列表中只显示当前学期的课程
5. THE System SHALL 确保课程的学期关联不能为空

### 需求 9：学期数据统计

**用户故事：** 作为学生用户，我希望看到每个学期的数据统计，以便了解学期的课程安排情况。

#### 验收标准

1. THE System SHALL 显示每个学期的课程总数
2. THE System SHALL 显示每个学期的周数范围
3. THE System SHALL 显示学期的时间跨度（开始日期到结束日期）
4. THE System SHALL 计算并显示学期的总学时
5. THE System SHALL 在学期列表中以卡片形式展示这些统计信息

### 需求 10：默认学期处理

**用户故事：** 作为开发者，我需要为首次使用的用户创建默认学期，以便用户可以立即开始使用课表功能。

#### 验收标准

1. WHEN 用户首次打开应用，THE System SHALL 自动创建默认学期
2. THE System SHALL 将默认学期命名为"当前学期"
3. THE System SHALL 将默认学期的开始日期设置为当前日期所在周的周一
4. THE System SHALL 将默认学期的总周数设置为18周
5. THE System SHALL 将所有现有课程关联到默认学期

### 需求 11：学期复制功能

**用户故事：** 作为学生用户，我希望能够复制已有学期的课表到新学期，以便快速创建相似的课程安排。

#### 验收标准

1. THE System SHALL 在学期列表中提供"复制学期"按钮
2. WHEN 用户点击复制，THE System SHALL 显示新学期创建对话框
3. THE System SHALL 预填充新学期名称（如"2024秋季学期 - 副本"）
4. WHEN 用户确认创建，THE System SHALL 复制所有课程到新学期
5. THE System SHALL 保持课程的所有属性（时间、地点、教师等）
6. THE System SHALL 自动切换到新创建的学期

### 需求 12：学期快速切换

**用户故事：** 作为学生用户，我希望能够快速在最近使用的学期之间切换，以便提高操作效率。

#### 验收标准

1. THE System SHALL 在课程表界面提供学期快速切换下拉菜单
2. THE System SHALL 在下拉菜单中显示最近使用的3个学期
3. WHEN 用户选择学期，THE System SHALL 立即切换并刷新课表
4. THE System SHALL 记录学期访问历史
5. THE System SHALL 在下拉菜单底部提供"查看所有学期"选项

### 需求 13：数据迁移

**用户故事：** 作为开发者，我需要确保现有用户的课程数据能够平滑迁移到多学期系统，以便用户不会丢失数据。

#### 验收标准

1. THE System SHALL 提供数据库迁移脚本
2. THE System SHALL 为所有现有课程创建默认学期
3. THE System SHALL 将所有现有课程关联到默认学期
4. THE System SHALL 保持现有课程的所有属性不变
5. THE System SHALL 确保迁移后应用正常运行

### 需求 14：国际化支持

**用户故事：** 作为多语言用户，我希望学期管理功能支持多语言，以便使用我熟悉的语言。

#### 验收标准

1. THE System SHALL 为所有学期管理相关UI文本提供国际化字符串
2. THE System SHALL 支持中文、英文、日文、越南文
3. THE System SHALL 确保学期名称可以使用任何语言输入
4. THE System SHALL 正确显示不同语言的日期格式
5. THE System SHALL 在所有支持的语言中提供一致的用户体验
