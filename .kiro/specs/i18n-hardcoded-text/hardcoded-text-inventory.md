# 硬编码文本清单

本文档记录了 Saison 应用中所有需要国际化的硬编码文本。

## 任务模块 (Task Module)

### TaskListScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 47 | "快速添加" | task_action_quick_add | 按钮文本 |
| 148 | "搜索任务..." | task_search_placeholder | 占位符 |
| 158 | "任务" | nav_tasks | 标题 |
| 165 | "关闭搜索" | cd_close_search | ContentDescription |
| 165 | "搜索" | cd_search | ContentDescription |
| 169 | "筛选" | cd_filter | ContentDescription |
| 172 | "日历" | cd_calendar | ContentDescription |
| 195 | "待完成" | task_stats_incomplete | 统计标签 |
| 203 | "已逾期" | task_stats_overdue | 统计标签 |
| 257 | "全部" | task_filter_all | 筛选器 |
| 265 | "进行中" | task_filter_active | 筛选器 |
| 273 | "已完成" | task_filter_completed | 筛选器 |
| 293 | "还没有任务" | task_empty_no_tasks | 空状态 |
| 294 | "没有进行中的任务" | task_empty_no_active | 空状态 |
| 295 | "还没有完成的任务" | task_empty_no_completed | 空状态 |
| 301 | "点击右下角按钮添加新任务" | task_empty_hint | 提示 |
| 321 | "未找到相关任务" | task_search_no_results | 搜索结果 |
| 326 | "搜索: \"%s\"" | task_search_query_format | 格式化文本 |
| 343 | "快速添加任务" | task_dialog_quick_add_title | 对话框标题 |
| 347 | "使用自然语言描述任务，例如：" | task_dialog_example_hint | 提示 |
| 351 | "\"明天下午3点开会 #工作\"" | task_dialog_example_text | 示例 |
| 357 | "任务描述" | task_dialog_description_label | 标签 |
| 367 | "添加" | common_action_add | 按钮 |
| 373 | "取消" | common_action_cancel | 按钮 |

### TaskDetailScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 28 | "任务详情" | task_detail_title | 标题 |
| 30 | "返回" | cd_back | ContentDescription |
| 34 | "删除" | cd_delete | ContentDescription |
| 60 | "任务不存在" | task_error_not_found | 错误消息 |
| 84 | "标题" | task_field_title | 字段标签 |
| 94 | "描述" | task_field_description | 字段标签 |
| 105 | "优先级" | task_field_priority | 字段标签 |
| 124 | "截止日期" | task_field_due_date | 字段标签 |
| 128 | "未设置" | task_due_date_not_set | 状态文本 |
| 139 | "位置" | task_field_location | 字段标签 |
| 150 | "子任务" | task_field_subtasks | 字段标签 |

### TaskCard.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 67 | "已完成" | cd_task_completed | ContentDescription |
| 74 | "未完成" | cd_task_incomplete | ContentDescription |
| 197 | "今天 %s" | time_today_at | 时间格式 |
| 198 | "明天 %s" | time_tomorrow_at | 时间格式 |
| 199 | "昨天 %s" | time_yesterday_at | 时间格式 |

## 设置模块 (Settings Module)

### SettingsScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 82 | "设置" | settings_title | 标题 |
| 84 | "返回" | cd_back | ContentDescription |
| 100 | "外观" | settings_section_appearance | 分组标题 |
| 102 | "主题" | settings_theme_title | 设置项 |
| 107 | "深色模式" | settings_dark_mode_title | 设置项 |
| 108 | "使用深色主题" | settings_dark_mode_subtitle | 副标题 |
| 114 | "底部导航栏" | settings_bottom_nav_title | 设置项 |
| 115 | "自定义显示的导航项" | settings_bottom_nav_subtitle | 副标题 |
| 120 | "语言" | settings_section_language | 分组标题 |
| 122 | "应用语言" | settings_language_title | 设置项 |
| 129 | "通知" | settings_section_notifications | 分组标题 |
| 134 | "启用通知" | settings_notifications_enable_title | 设置项 |
| 135 | "接收任务和课程提醒" | settings_notifications_enable_subtitle | 副标题 |
| 141 | "任务提醒" | settings_task_reminders_title | 设置项 |
| 142 | "接收任务截止日期提醒" | settings_task_reminders_subtitle | 副标题 |
| 149 | "课程提醒" | settings_course_reminders_title | 设置项 |
| 150 | "接收课程开始提醒" | settings_course_reminders_subtitle | 副标题 |
| 157 | "番茄钟提醒" | settings_pomodoro_reminders_title | 设置项 |
| 158 | "接收番茄钟完成提醒" | settings_pomodoro_reminders_subtitle | 副标题 |
| 166 | "同步" | settings_section_sync | 分组标题 |
| 171 | "自动同步" | settings_auto_sync_title | 设置项 |
| 172 | "自动同步到 WebDAV" | settings_auto_sync_subtitle | 副标题 |
| 178 | "仅 WiFi 同步" | settings_wifi_only_title | 设置项 |
| 179 | "仅在 WiFi 网络下同步" | settings_wifi_only_subtitle | 副标题 |
| 186 | "WebDAV 配置" | settings_webdav_title | 设置项 |
| 187 | "配置 WebDAV 服务器" | settings_webdav_subtitle | 副标题 |
| 200 | "立即同步" | settings_sync_now_title | 设置项 |
| 206 | "刚刚" | time_just_now | 时间 |
| 207 | "%d 分钟前" | time_minutes_ago | 时间格式 |
| 208 | "%d 小时前" | time_hours_ago | 时间格式 |
| 209 | "%d 天前" | time_days_ago | 时间格式 |
| 211 | "上次同步: %s" | settings_sync_last_time | 格式化文本 |
| 213 | "尚未同步" | settings_sync_not_yet | 状态 |
| 217 | "错误: %s" | settings_sync_error | 错误格式 |
| 233 | "立即同步" | cd_sync_now | ContentDescription |
| 240 | "同步" | settings_sync_button | 按钮 |
| 250 | "番茄钟" | settings_section_pomodoro | 分组标题 |
| 256 | "番茄钟时长" | settings_pomodoro_duration_title | 设置项 |
| 257 | "工作: %1$dmin \| 短休息: %2$dmin \| 长休息: %3$dmin" | settings_pomodoro_duration_subtitle | 副标题格式 |
| 263 | "节拍器" | settings_section_metronome | 分组标题 |
| 268 | "节拍器设置" | settings_metronome_title | 设置项 |
| 269 | "默认 BPM: %d" | settings_metronome_subtitle | 副标题格式 |
| 274 | "关于" | settings_section_about | 分组标题 |
| 276 | "关于 Saison" | settings_about_title | 设置项 |
| 277 | "版本 1.0.0" | settings_about_subtitle | 副标题 |
| 281 | "开源许可" | settings_licenses_title | 设置项 |
| 282 | "查看开源许可证" | settings_licenses_subtitle | 副标题 |
| 520 | "选择主题" | settings_theme_dialog_title | 对话框标题 |
| 545 | "关闭" | common_action_close | 按钮 |
| 554 | "选择语言" | settings_language_dialog_title | 对话框标题 |
| 557 | "简体中文" | language_zh_cn | 语言名称 |
| 558 | "English" | language_en | 语言名称 |
| 559 | "日本語" | language_ja | 语言名称 |
| 560 | "Tiếng Việt" | language_vi | 语言名称 |
| 593 | "WebDAV 配置" | settings_webdav_dialog_title | 对话框标题 |
| 600 | "服务器地址" | settings_webdav_url_label | 标签 |
| 601 | "https://example.com/webdav" | settings_webdav_url_placeholder | 占位符 |
| 604 | "请输入有效的 URL（http:// 或 https://）" | settings_webdav_url_error | 错误消息 |
| 610 | "用户名" | settings_webdav_username_label | 标签 |
| 617 | "密码" | settings_webdav_password_label | 标签 |
| 626 | "隐藏密码" | cd_hide_password | ContentDescription |
| 626 | "显示密码" | cd_show_password | ContentDescription |
| 636 | "测试连接" | settings_webdav_test_button | 按钮 |
| 651 | "✓ 连接成功" | settings_webdav_test_success | 成功消息 |
| 658 | "✗ %s" | settings_webdav_test_error | 错误格式 |
| 671 | "保存" | common_action_save | 按钮 |
| 676 | "取消" | common_action_cancel | 按钮 |

## 番茄钟模块 (Pomodoro Module)

### PomodoroScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 37 | "番茄钟" | pomodoro_title | 标题 |
| 39 | "设置" | cd_settings | ContentDescription |
| 117 | "%d 个番茄钟" | pomodoro_count_format | 格式化文本 |
| 138 | "开始专注" | pomodoro_action_start | 按钮 |
| 147 | "暂停" | pomodoro_action_pause | 按钮 |
| 163 | "继续" | pomodoro_action_resume | 按钮 |
| 193 | "今日统计" | pomodoro_stats_today | 标题 |
| 202 | "完成" | pomodoro_stats_completed | 统计标签 |
| 208 | "专注时长" | pomodoro_stats_duration | 统计标签 |
| 209 | "%d 分钟" | pomodoro_stats_minutes_format | 格式化文本 |
| 213 | "中断" | pomodoro_stats_interruptions | 统计标签 |
| 252 | "番茄钟设置" | pomodoro_settings_dialog_title | 对话框标题 |
| 257 | "工作时长: %d 分钟" | pomodoro_work_duration_format | 格式化文本 |
| 270 | "确定" | common_action_confirm | 按钮 |
| 276 | "取消" | common_action_cancel | 按钮 |

## 节拍器模块 (Metronome Module)

### MetronomeScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 43 | "节拍器" | metronome_title | 标题 |
| 45 | "预设" | metronome_presets | 按钮 |
| 48 | "保存预设" | metronome_save_preset | 按钮 |
| 51 | "设置" | cd_settings | ContentDescription |
| 82 | "减少 1" | metronome_decrease_1 | ContentDescription |
| 87 | "减少 5" | metronome_decrease_5 | ContentDescription |
| 92 | "减少 10" | metronome_decrease_10 | ContentDescription |
| 135 | "增加 1" | metronome_increase_1 | ContentDescription |
| 140 | "增加 5" | metronome_increase_5 | ContentDescription |
| 145 | "增加 10" | metronome_increase_10 | ContentDescription |
| 195 | "播放" | cd_play | ContentDescription |
| 195 | "暂停" | cd_pause | ContentDescription |
| 218 | "拍号" | metronome_time_signature | 标签 |
| 219 | "当前拍" | metronome_current_beat | 标签 |
| 220 | "总节拍" | metronome_total_beats | 标签 |
| 280 | "节拍器设置" | metronome_settings_dialog_title | 对话框标题 |
| 285 | "拍号" | metronome_time_signature_label | 标签 |
| 299 | "音量" | metronome_volume_label | 标签 |
| 323 | "第一拍重音" | metronome_accent_first_title | 设置项 |
| 324 | "强调每小节的第一拍" | metronome_accent_first_subtitle | 副标题 |
| 337 | "振动反馈" | metronome_vibration_title | 设置项 |
| 338 | "每次节拍时提供触觉反馈" | metronome_vibration_subtitle | 副标题 |
| 350 | "确定" | common_action_confirm | 按钮 |
| 360 | "我的预设" | metronome_presets_dialog_title | 对话框标题 |
| 364 | "还没有保存的预设\n点击顶部保存按钮创建预设" | metronome_presets_empty | 空状态 |
| 391 | "%d BPM • %d/4" | metronome_preset_info_format | 格式化文本 |
| 397 | "删除" | cd_delete | ContentDescription |
| 410 | "关闭" | common_action_close | 按钮 |
| 420 | "保存预设" | metronome_save_preset_dialog_title | 对话框标题 |
| 425 | "为当前设置命名" | metronome_save_preset_hint | 提示 |
| 431 | "预设名称" | metronome_preset_name_label | 标签 |
| 432 | "例如：练习曲 120" | metronome_preset_name_placeholder | 占位符 |
| 444 | "保存" | common_action_save | 按钮 |
| 451 | "取消" | common_action_cancel | 按钮 |
| 461-470 | 速度标签 | tempo_* | 音乐术语 |

## 课程模块 (Course Module)

### CourseScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 35 | "添加课程" | course_action_add | 按钮 |
| 78 | "课程表" | course_title | 标题 |
| 83 | "第 %d 周" | course_week_format | 格式化文本 |
| 88 | "OCR 导入" | cd_ocr_import | ContentDescription |
| 91 | "周模式" | cd_week_pattern | ContentDescription |
| 105 | "全部" | course_pattern_all | 周模式 |
| 106 | "A周" | course_pattern_a | 周模式 |
| 107 | "B周" | course_pattern_b | 周模式 |
| 108 | "单周" | course_pattern_odd | 周模式 |
| 109 | "双周" | course_pattern_even | 周模式 |
| 148 | "%d 节课" | course_count_format | 格式化文本 |
| 175 | "还没有课程" | course_empty_no_courses | 空状态 |
| 180 | "点击右下角按钮添加课程" | course_empty_hint | 提示 |
| 188-195 | 星期名称 | day_* | 日期 |

### CourseCard.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 107-113 | 周模式标签 | course_pattern_* | 标签 |

## 日历模块 (Calendar Module)

### CalendarScreen.kt

| 行号范围 | 硬编码文本 | 建议资源ID | 类型 |
|---------|-----------|-----------|------|
| 82 | "上一个" | cd_previous | ContentDescription |
| 88 | "下一个" | cd_next | ContentDescription |
| 93 | "今天" | cd_today | ContentDescription |
| 113-116 | 视图模式 | calendar_view_* | 标签 |

## 总结

### 按模块统计

| 模块 | 硬编码文本数量 | 优先级 |
|------|--------------|--------|
| 任务模块 | 35+ | 高 |
| 设置模块 | 60+ | 高 |
| 番茄钟模块 | 20+ | 中 |
| 节拍器模块 | 35+ | 中 |
| 课程模块 | 20+ | 中 |
| 日历模块 | 10+ | 低 |
| **总计** | **180+** | - |

### 按类型统计

| 类型 | 数量 | 说明 |
|------|------|------|
| UI 标签 | 60+ | 按钮、标题、字段标签等 |
| 提示消息 | 30+ | 空状态、错误消息、提示文本 |
| ContentDescription | 25+ | 无障碍描述 |
| 格式化文本 | 20+ | 包含参数的动态文本 |
| 对话框文本 | 25+ | 对话框标题、按钮等 |
| 状态文本 | 20+ | 时间、统计等状态显示 |

### 需要特殊处理的文本

1. **参数化字符串**：
   - 时间格式（今天、明天、X分钟前等）
   - 数量格式（%d 个番茄钟、%d 节课等）
   - 复合格式（工作: %1$dmin | 短休息: %2$dmin）

2. **日期和时间**：
   - 星期名称（周一到周日）
   - 月份名称
   - 时间格式（HH:mm、yyyy-MM-dd等）

3. **音乐术语**：
   - 速度标签（极慢板、广板、慢板等）
   - 这些术语在不同语言中有标准翻译

4. **技术术语**：
   - WebDAV、OCR、BPM 等保持英文
   - URL、WiFi 等保持原样

## 下一步行动

1. 为每个模块创建字符串资源
2. 为所有语言提供翻译
3. 替换代码中的硬编码文本
4. 验证和测试
