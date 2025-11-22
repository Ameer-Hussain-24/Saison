# Implementation Plan - Emoji to Material 3 Icons Migration

- [x] 1. 替换TaskListScreen中的emoji图标


  - 在TaskListScreen.kt中导入必要的Material Icons
  - 将EmptyTaskList函数中的emoji "✨" 替换为Icons.Outlined.AutoAwesome
  - 将EmptySearchResult函数中的emoji "🔍" 替换为Icons.Outlined.SearchOff
  - 应用正确的图标尺寸（64dp）和主题颜色
  - _Requirements: 1.4, 1.5, 2.1, 3.1, 4.1_



- [x] 2. 替换CourseScreen中的emoji图标


  - 在CourseScreen.kt中导入必要的Material Icons
  - 将EmptyCourseList函数中的emoji "📚" 替换为Icons.Outlined.MenuBook
  - 应用正确的图标尺寸（64dp）和主题颜色

  - 确保图标与周围UI元素对齐良好

  - _Requirements: 2.2, 3.2, 4.1_



- [ ] 3. 验证和优化图标显示效果
  - 在浅色模式下验证所有图标显示效果



  - 在深色模式下验证所有图标颜色自动适配
  - 验证图标尺寸与文本大小协调


  - 确保图标间距和对齐符合Material 3规范
  - _Requirements: 1.3, 2.3, 2.4, 4.2, 4.3, 4.4_

- [ ] 4. 代码质量检查
  - 移除未使用的emoji相关代码
  - 确保所有图标导入语句正确
  - 验证代码可读性和可维护性提升
  - 添加必要的代码注释说明图标选择理由
  - _Requirements: 3.3, 3.4, 3.5_
