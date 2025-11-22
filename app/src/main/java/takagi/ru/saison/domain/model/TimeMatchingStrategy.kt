package takagi.ru.saison.domain.model

/**
 * 时间匹配策略
 */
sealed class TimeMatchingStrategy {
    /**
     * 使用现有节次配置
     * 将导入的课程时间匹配到当前学期的节次设置
     */
    object UseExistingPeriods : TimeMatchingStrategy()
    
    /**
     * 根据导入文件自动创建节次
     * 从ICS文件中的时间自动生成新的节次配置
     */
    object AutoCreatePeriods : TimeMatchingStrategy()
}
