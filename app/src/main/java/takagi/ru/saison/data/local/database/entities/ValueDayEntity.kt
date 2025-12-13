package takagi.ru.saison.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 买断实体：记录购买的商品，并可计算每日均价
 *
 * @property id 主键ID
 * @property itemName 商品名称
 * @property purchasePrice 购买价格
 * @property purchaseDate 购买日期
 * @property category 分类
 * @property warrantyEndDate 保修到期日期（可选）
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Entity(tableName = "value_days")
data class ValueDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val purchasePrice: Double,
    val purchaseDate: Long,  // 使用 Long (epoch day) 存储日期
    val category: String = "未分类",
    val warrantyEndDate: Long? = null,  // 保修到期日期 (epoch day)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取购买日期的 LocalDate 对象
     */
    fun getPurchaseDateAsLocalDate(): LocalDate {
        return LocalDate.ofEpochDay(purchaseDate)
    }
    
    /**
     * 计算从购买日期到今天的天数
     */
    fun getDaysSincePurchase(): Long {
        val today = LocalDate.now()
        val purchaseDateLocal = LocalDate.ofEpochDay(purchaseDate)
        return java.time.temporal.ChronoUnit.DAYS.between(purchaseDateLocal, today) + 1 // +1 包含购买当天
    }
    
    /**
     * 计算每日均价
     */
    fun getDailyAverageCost(): Double {
        val days = getDaysSincePurchase()
        return if (days > 0) purchasePrice / days else purchasePrice
    }
    
    /**
     * 获取保修到期日期的 LocalDate 对象
     */
    fun getWarrantyEndDateAsLocalDate(): LocalDate? {
        return warrantyEndDate?.let { LocalDate.ofEpochDay(it) }
    }
    
    /**
     * 检查保修是否已过期
     */
    fun isWarrantyExpired(): Boolean {
        val endDate = getWarrantyEndDateAsLocalDate() ?: return false
        return LocalDate.now().isAfter(endDate)
    }
    
    /**
     * 获取保修剩余天数
     */
    fun getWarrantyDaysRemaining(): Long? {
        val endDate = getWarrantyEndDateAsLocalDate() ?: return null
        val today = LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, endDate)
        return if (days >= 0) days else 0
    }
}
