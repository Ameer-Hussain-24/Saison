package takagi.ru.saison.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MetronomePreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val bpm: Int,
    val timeSignature: Int,
    val volume: Float,
    val accentFirstBeat: Boolean,
    val enableVibration: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
