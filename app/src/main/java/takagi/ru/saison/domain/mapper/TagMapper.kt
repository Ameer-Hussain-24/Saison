package takagi.ru.saison.domain.mapper

import takagi.ru.saison.data.local.database.entities.TagEntity
import takagi.ru.saison.domain.model.Tag

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        path = path,
        parentId = parentId,
        icon = icon,
        color = color,
        createdAt = createdAt
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        path = path,
        parentId = parentId,
        icon = icon,
        color = color,
        createdAt = createdAt
    )
}
