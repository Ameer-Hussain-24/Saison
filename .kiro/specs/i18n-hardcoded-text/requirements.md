# 需求文档

## 简介

本功能旨在检测并修复 Saison 应用中所有硬编码的文本字符串，确保应用完全支持多语言国际化（i18n）。当前应用已经建立了基本的多语言框架（支持英语、中文、日语、越南语），但代码中仍存在硬编码文本，这些文本未使用 Android 字符串资源系统，导致无法根据用户语言设置自动切换。

## 术语表

- **Application**: Saison 任务管理应用
- **Hardcoded Text**: 直接写在 Kotlin 代码中的字符串文字，而非通过字符串资源引用
- **String Resource**: Android 的 XML 字符串资源文件（strings.xml）
- **i18n System**: 应用的国际化系统，包括字符串资源文件和 LocaleHelper
- **UI Component**: 用户界面组件，包括 Screen、Dialog、Card 等 Composable 函数
- **User**: 应用的最终用户

## 需求

### 需求 1：检测硬编码文本

**用户故事：** 作为开发者，我希望能够识别应用中所有硬编码的文本，以便进行系统化的国际化改造

#### 验收标准

1. THE Application SHALL 扫描所有 Kotlin 源文件中的硬编码文本字符串
2. WHEN 发现硬编码文本时，THE Application SHALL 记录文件路径、行号和文本内容
3. THE Application SHALL 区分需要国际化的用户可见文本和不需要国际化的技术字符串（如日志标签、键名）
4. THE Application SHALL 生成硬编码文本清单，按文件和功能模块分类

### 需求 2：创建缺失的字符串资源

**用户故事：** 作为开发者，我希望为所有硬编码文本创建对应的字符串资源，以便支持多语言

#### 验收标准

1. WHEN 识别出硬编码文本时，THE Application SHALL 在 values/strings.xml 中创建对应的字符串资源条目
2. THE Application SHALL 为每个字符串资源分配语义化的资源 ID（如 "error_network_failed"）
3. THE Application SHALL 确保新增的字符串资源 ID 不与现有资源冲突
4. THE Application SHALL 按功能模块和类型组织字符串资源（使用 XML 注释分组）
5. THE Application SHALL 保持字符串资源文件的格式一致性和可读性

### 需求 3：翻译字符串资源

**用户故事：** 作为用户，我希望应用界面能够显示我的母语，以便更好地理解和使用应用

#### 验收标准

1. WHEN 在 values/strings.xml 中添加新字符串时，THE Application SHALL 在所有支持的语言文件中添加对应的翻译
2. THE Application SHALL 为以下语言提供翻译：中文（zh-rCN）、日语（ja）、越南语（vi）
3. THE Application SHALL 确保翻译准确传达原文含义并符合目标语言的表达习惯
4. THE Application SHALL 保持各语言文件中字符串资源的顺序和结构一致
5. WHERE 某些术语或品牌名称不需要翻译，THE Application SHALL 在所有语言文件中保持一致

### 需求 4：替换代码中的硬编码文本

**用户故事：** 作为开发者，我希望将代码中的硬编码文本替换为字符串资源引用，以便实现动态语言切换

#### 验收标准

1. THE Application SHALL 将所有硬编码文本替换为 stringResource() 函数调用
2. WHEN 在 Composable 函数中使用字符串时，THE Application SHALL 使用 stringResource(R.string.resource_id)
3. WHEN 在 ViewModel 或其他非 Composable 类中使用字符串时，THE Application SHALL 通过 Context 获取字符串资源
4. THE Application SHALL 确保替换后的代码能够正确编译和运行
5. THE Application SHALL 保持代码的可读性和维护性

### 需求 5：验证国际化实现

**用户故事：** 作为质量保证人员，我希望验证所有文本都已正确国际化，以确保用户体验的一致性

#### 验收标准

1. THE Application SHALL 在所有支持的语言环境下正确显示翻译后的文本
2. WHEN 用户切换语言设置时，THE Application SHALL 立即或在重启后显示新语言的文本
3. THE Application SHALL 确保没有遗漏的硬编码文本显示在用户界面上
4. THE Application SHALL 确保所有字符串资源都有对应的翻译（无缺失的翻译条目）
5. THE Application SHALL 确保文本在不同语言下的 UI 布局正常（无文本截断或溢出）

### 需求 6：建立国际化最佳实践

**用户故事：** 作为开发团队成员，我希望有明确的国际化指南，以便在未来开发中避免硬编码文本

#### 验收标准

1. THE Application SHALL 提供国际化开发指南文档
2. THE Application SHALL 在代码审查清单中包含国际化检查项
3. WHERE 需要添加新的用户可见文本时，THE Application SHALL 要求开发者首先创建字符串资源
4. THE Application SHALL 使用静态代码分析工具检测新引入的硬编码文本
5. THE Application SHALL 定期审查和更新翻译质量
