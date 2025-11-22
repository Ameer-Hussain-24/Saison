# Implementation Plan

- [x] 1. 更新CalendarViewMode枚举


  - 将AGENDA改为LIST
  - 更新所有引用AGENDA的代码位置
  - _Requirements: 3.1, 3.2_

- [x] 2. 更新字符串资源


  - [x] 2.1 更新中文字符串资源


    - 将"议程"改为"列表"
    - 更新calendar_view相关的字符串
    - _Requirements: 3.1, 3.3_
  
  - [x] 2.2 更新其他语言字符串资源


    - 更新英文、日文、越南文的对应字符串
    - 确保所有语言的视图名称一致
    - _Requirements: 3.3_

- [x] 3. 扩展CalendarViewModel的任务显示逻辑



  - [x] 3.1 修改loadEvents()方法


    - 添加已完成任务的显示逻辑
    - 没有截止日期但已完成的任务显示在完成日期
    - 提取createEventFromTask()辅助方法
    - _Requirements: 1.1, 1.2, 2.1, 2.2, 4.1, 4.2_
  
  - [x] 3.2 添加任务操作方法


    - 实现toggleTaskCompletion()方法
    - 实现deleteTask()方法
    - 添加导航事件处理
    - _Requirements: 5.1, 5.3, 5.4_

- [x] 4. 重命名AgendaView为ListView


  - [x] 4.1 重命名文件和组件


    - 将AgendaView.kt重命名为ListView.kt
    - 更新组件名称和函数名
    - _Requirements: 3.1, 3.4_
  
  - [x] 4.2 更新ListView的任务卡片显示

    - 添加已完成任务的视觉标识（半透明、删除线）
    - 区分有截止日期和完成日期的任务
    - 添加优先级颜色指示
    - _Requirements: 1.3, 2.3, 2.4_
  
  - [x] 4.3 添加任务卡片交互

    - 实现点击导航到任务详情
    - 实现长按显示快速操作菜单
    - 添加快速完成/删除操作
    - _Requirements: 1.4, 5.1, 5.2, 5.3_

- [x] 5. 更新CalendarScreen

  - [x] 5.1 更新视图模式切换UI

    - 更新视图模式按钮文本
    - 将AGENDA改为LIST
    - _Requirements: 3.2, 3.4_
  
  - [x] 5.2 添加任务详情导航处理

    - 实现从日历到任务详情的导航
    - 处理任务不存在的错误情况
    - _Requirements: 5.1_
  
  - [x] 5.3 更新ListView的引用

    - 将AgendaView改为ListView
    - 确保所有视图切换正常工作
    - _Requirements: 3.1, 3.4_

- [x] 6. 更新MonthView显示已完成任务





  - [x] 6.1 添加已完成任务的视觉标识


    - 使用半透明样式
    - 添加完成图标
    - _Requirements: 2.3, 2.4_
  
  - [x] 6.2 添加任务卡片点击处理


    - 实现点击导航
    - 实现长按菜单
    - _Requirements: 1.4, 5.1, 5.2_

- [x] 7. 更新WeekView显示已完成任务



  - [x] 7.1 添加已完成任务的视觉标识

    - 使用半透明样式
    - 添加完成图标
    - _Requirements: 2.3, 2.4_
  

  - [x] 7.2 添加任务卡片点击处理

    - 实现点击导航
    - 实现长按菜单
    - _Requirements: 1.4, 5.1, 5.2_

- [x] 8. 更新DayView显示已完成任务



  - [x] 8.1 添加已完成任务的视觉标识

    - 使用半透明样式
    - 添加完成图标
    - _Requirements: 2.3, 2.4_
  


  - [x] 8.2 添加任务卡片点击处理


    - 实现点击导航
    - 实现长按菜单
    - _Requirements: 1.4, 5.1, 5.2_

- [x] 9. 测试和验证



  - [x] 9.1 验证任务显示逻辑

    - 测试有截止日期的任务显示
    - 测试已完成无截止日期的任务显示
    - 测试任务在正确日期显示
    - _Requirements: 1.1, 1.2, 2.1, 2.2_
  
  - [x] 9.2 验证视图命名更新

    - 检查所有语言的视图名称
    - 确认"列表"替代"议程"
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [x] 9.3 验证任务交互功能

    - 测试点击导航到详情
    - 测试长按快速操作
    - 测试任务完成/删除
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
