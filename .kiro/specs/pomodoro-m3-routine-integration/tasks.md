# 实现计划

- [x] 1. 数据层基础设施




  - 扩展PomodoroSession数据模型，添加routineTaskId、actualDuration和isEarlyFinish字段
  - 创建数据库迁移脚本，更新pomodoro_sessions表结构
  - 在RoutineRepository中添加getRoutineTasksWithDuration方法，过滤返回设置了活动时长的任务
  - 在RoutineRepository中添加checkInWithNote方法，支持带备注的打卡记录创建
  - _需求: 3.7, 4.1, 4.3, 5.7_

- [x] 2. 通知和震动管理器




- [x] 2.1 实现PomodoroNotificationManager




  - 创建PomodoroNotificationManager类，使用SoundPool管理音频播放
  - 实现initialize、playCompletionSound和release方法
  - 使用ToneGenerator作为音频播放的后备方案
  - 添加Hilt依赖注入支持
  - _需求: 2.1_

- [x] 2.2 实现VibrationManager




  - 创建VibrationManager类，封装设备震动功能
  - 实现vibrateCompletion方法，使用预定义的震动模式
  - 检查VIBRATE权限
  - 添加Hilt依赖注入支持
  - _需求: 2.2_

- [x] 2.3 扩展PreferencesManager




  - 添加pomodoroSoundEnabled和pomodoroVibrationEnabled设置键
  - 实现getPomodoroSoundEnabled和setPomodoroSoundEnabled方法
  - 实现getPomodoroVibrationEnabled和setPomodoroVibrationEnabled方法
  - 设置默认值为true（启用）
  - _需求: 2.3, 2.4, 7.1, 7.2_

- [x] 3. ViewModel状态管理




- [x] 3.1 更新PomodoroViewModel状态



  - 创建PomodoroUiState数据类，包含selectedRoutineTask字段
  - 创建PomodoroSettings数据类，包含soundEnabled和vibrationEnabled字段
  - 更新TimerState密封类，在Completed状态中添加isEarlyFinish标志
  - 使用StateFlow管理UI状态
  - _需求: 3.5, 6.5_

- [x] 3.2 实现任务选择逻辑


  - 在ViewModel中添加selectRoutineTask方法
  - 实现loadAvailableRoutineTasks方法，调用Repository获取有时长的任务
  - 在选择任务时更新totalSeconds为任务的活动时长
  - 允许在空闲状态下更改选择的任务
  - _需求: 3.1, 3.4, 3.6_

- [x] 3.3 实现自动打卡逻辑


  - 在onTimerComplete方法中检查是否选择了日程任务
  - 如果选择了任务，调用RoutineRepository.checkInWithNote创建打卡记录
  - 在打卡记录备注中添加"通过番茄钟完成"标识
  - 处理打卡成功和失败的情况，显示相应的提示消息
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 3.4 实现提前结束逻辑


  - 添加earlyFinish方法，接收markComplete参数
  - 计算实际用时（totalSeconds - remainingSeconds）
  - 如果markComplete为true且选择了任务，创建带"提前结束"备注的打卡记录
  - 停止计时器并更新状态为Completed(isEarlyFinish = true)
  - _需求: 5.4, 5.5, 5.7_

- [x] 3.5 集成通知和震动


  - 在ViewModel中注入PomodoroNotificationManager和VibrationManager
  - 在onTimerComplete中根据设置播放声音和触发震动
  - 从PreferencesManager读取声音和震动开关状态
  - 实现updateSettings方法保存用户设置
  - _需求: 2.1, 2.2, 2.5, 2.6, 7.5_

- [x] 4. UI组件 - 任务选择




- [x] 4.1 创建RoutineTaskSelector组件




  - 使用ModalBottomSheet实现任务选择器
  - 使用LazyColumn显示任务列表
  - 每个任务项显示图标、标题和活动时长
  - 实现空状态UI（无可用任务时）
  - _需求: 3.1, 3.2, 3.3_

- [x] 4.2 创建RoutineTaskCard组件




  - 使用Card组件显示选中的任务信息
  - 显示任务图标（32dp）、标题和活动时长
  - 使用primaryContainer作为背景色
  - 添加"更换任务"按钮
  - _需求: 3.5, 6.3, 6.4_

- [x] 5. UI组件 - 计时器界面




- [x] 5.1 重设计TimerDisplay组件




  - 使用Material 3颜色系统（primary/tertiary/secondary）
  - 实现环形进度指示器，使用CircularProgressIndicator
  - 中心显示剩余时间（MM:SS格式）
  - 根据状态（运行/暂停/完成）使用不同颜色
  - 添加完成动画效果
  - _需求: 1.1, 6.1, 6.2, 6.5, 6.6_

- [x] 5.2 更新TimerControls组件




  - 应用Material 3按钮样式（FilledButton/OutlinedButton/TextButton）
  - 在运行状态下显示"提前结束"按钮
  - 使用Material Icons替换所有图标
  - 应用4dp基准的间距系统
  - _需求: 1.4, 5.1_

- [x] 5.3 创建EarlyFinishDialog组件




  - 使用AlertDialog实现提前结束确认对话框
  - 显示已用时间信息
  - 提供三个选项：标记完成、仅停止、取消
  - 应用Material 3对话框样式
  - _需求: 5.2, 5.3, 5.6_

- [x] 6. UI组件 - 主屏幕和设置




- [x] 6.1 重设计PomodoroScreen




  - 应用Material 3颜色系统到所有UI元素
  - 使用RoundedCornerShape(16.dp)设计Card
  - 应用4dp基准的间距系统（8dp, 12dp, 16dp, 24dp）
  - 集成RoutineTaskCard和RoutineTaskSelector
  - 添加AnimatedVisibility动画效果
  - _需求: 1.1, 1.2, 1.3, 1.5_

- [x] 6.2 更新PomodoroSettingsSheet




  - 添加声音提醒开关（Switch）
  - 添加震动提醒开关（Switch）
  - 应用Material 3样式到所有设置项
  - 保持现有的时长设置（工作、短休息、长休息）
  - _需求: 2.3, 2.4_

- [x] 7. 国际化和资源



- [x] 7.1 添加字符串资源



  - 在values/strings.xml中添加所有新的字符串资源
  - 包括任务选择、提前结束、设置和通知相关文本
  - 添加格式化字符串（时长、用时等）
  - _需求: 所有需求_

- [x] 7.2 添加多语言翻译


  - 在values-zh-rCN/strings.xml中添加中文翻译
  - 在values-ja/strings.xml中添加日文翻译
  - 在values-vi/strings.xml中添加越南语翻译
  - _需求: 所有需求_

- [x] 8. 权限和清单


- [x] 8.1 更新AndroidManifest.xml


  - 添加VIBRATE权限声明
  - 添加POST_NOTIFICATIONS权限声明（Android 13+）
  - _需求: 2.2_

- [x] 9. 集成和测试



- [x] 9.1 端到端集成


  - 连接所有组件（UI、ViewModel、Repository）
  - 测试完整的任务选择到自动打卡流程
  - 测试提前结束流程
  - 验证通知和震动功能
  - _需求: 所有需求_

- [x] 9.2 错误处理和边界情况


  - 添加任务加载失败的错误处理
  - 添加打卡失败的重试机制
  - 处理权限被拒绝的情况
  - 测试无网络、数据库错误等边界情况
  - _需求: 4.5_

- [x] 9.3 性能优化


  - 优化任务列表加载性能
  - 优化计时器更新频率
  - 确保通知和震动不阻塞UI
  - 使用remember和derivedStateOf减少重组
  - _需求: 所有需求_

- [x] 9.4 无障碍测试


  - 为所有图标按钮添加contentDescription
  - 验证触摸目标大小（最小48dp）
  - 测试屏幕阅读器支持
  - 验证颜色对比度
  - _需求: 所有需求_
