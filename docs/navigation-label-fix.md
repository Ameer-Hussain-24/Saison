# 底部导航栏标签优化

## 问题

底部导航栏有6个项目，其中"Pomodoro"和"Metronome"这两个标签太长，导致：
- 文字换行或被截断
- 导航栏项目之间对齐不一致
- 视觉效果不佳

## 解决方案

缩短导航标签，使用更简洁的名称：

### 英语 (values/strings.xml)
- `Pomodoro` → `Timer` (计时器)
- `Metronome` → `Beat` (节拍)

### 中文 (values-zh-rCN/strings.xml)
- `番茄钟` → `专注` (更简洁，保持语义)
- `节拍器` → `节拍` (缩短)
- `课程表` → `课程` (额外优化)

### 日语 (values-ja/strings.xml)
- `ポモドーロ` → `集中` (专注)
- `メトロノーム` → `拍子` (节拍)
- `時間割` → `授業` (课程)

### 越南语 (values-vi/strings.xml)
- `Pomodoro` → `Tập trung` (专注)
- `Nhịp điệu` → `Nhịp` (节拍)
- `Thời khóa biểu` → `Khóa học` (课程)
- `Nhiệm vụ` → `Việc` (任务，更简洁)

## 改进效果

✅ 所有导航标签长度更均衡
✅ 文字不再换行或被截断
✅ 导航栏视觉效果更整洁
✅ 保持了语义清晰度
✅ 多语言一致性

## 技术细节

- 修改文件：
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/values-zh-rCN/strings.xml`
  - `app/src/main/res/values-ja/strings.xml`
  - `app/src/main/res/values-vi/strings.xml`

- 构建状态：✅ 成功
- 无需修改代码逻辑
- 向后兼容

## 标签对比

| 功能 | 之前 (EN) | 现在 (EN) | 之前 (ZH) | 现在 (ZH) |
|------|-----------|-----------|-----------|-----------|
| Calendar | Calendar | Calendar | 日历 | 日历 |
| Course | Course | Course | 课程表 | 课程 |
| Tasks | Tasks | Tasks | 任务 | 任务 |
| Pomodoro | Pomodoro | **Timer** | 番茄钟 | **专注** |
| Metronome | Metronome | **Beat** | 节拍器 | **节拍** |
| Settings | Settings | Settings | 设置 | 设置 |

## 设计考虑

1. **简洁性**: 所有标签控制在2-3个字符（中文）或4-8个字符（英文）
2. **语义清晰**: 虽然缩短了，但仍然能清楚表达功能
3. **一致性**: 所有语言版本都进行了相应优化
4. **用户体验**: 更容易扫视和点击

## 未来建议

如果需要进一步优化，可以考虑：
1. 使用仅图标模式（隐藏标签）
2. 减少导航项数量（合并某些功能）
3. 使用抽屉式导航（Drawer）代替底部导航栏
