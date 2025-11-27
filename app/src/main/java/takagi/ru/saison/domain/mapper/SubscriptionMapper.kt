package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.SubscriptionEntity
import takagi.ru.saison.domain.model.BillingCycle
import takagi.ru.saison.domain.model.Subscription
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = id,
        name = name,
        description = note,
        price = price,
        currency = currency,
        billingCycle = when (cycleType) {
            "MONTHLY" -> BillingCycle.MONTHLY
            "QUARTERLY" -> BillingCycle.QUARTERLY
            "YEARLY" -> BillingCycle.YEARLY
            "WEEKLY" -> BillingCycle.WEEKLY
            else -> BillingCycle.MONTHLY
        },
        startDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate(),
        nextBillingDate = Instant.ofEpochMilli(nextRenewalDate).atZone(ZoneId.systemDefault()).toLocalDate(),
        reminderDaysBefore = reminderDaysBefore,
        isActive = isActive,
        category = category,
        icon = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        name = name,
        category = category ?: "",
        price = price,
        currency = currency,
        cycleType = when (billingCycle) {
            BillingCycle.MONTHLY -> "MONTHLY"
            BillingCycle.QUARTERLY -> "QUARTERLY"
            BillingCycle.YEARLY -> "YEARLY"
            BillingCycle.WEEKLY -> "WEEKLY"
        },
        cycleDuration = 1,
        startDate = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        nextRenewalDate = nextBillingDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        autoRenewal = true,
        reminderEnabled = reminderDaysBefore > 0,
        reminderDaysBefore = reminderDaysBefore,
        note = description,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
