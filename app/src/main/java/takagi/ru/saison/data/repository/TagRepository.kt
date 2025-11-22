package takagi.ru.saison.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.saison.data.local.database.dao.TagDao
import takagi.ru.saison.domain.mapper.toDomain
import takagi.ru.saison.domain.mapper.toEntity
import takagi.ru.saison.domain.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    
    fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTagsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getRootTags(): Flow<List<Tag>> {
        return tagDao.getRootTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getChildTags(parentId: Long): Flow<List<Tag>> {
        return tagDao.getChildTags(parentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getTagById(tagId: Long): Tag? {
        return tagDao.getTagById(tagId)?.toDomain()
    }
    
    suspend fun getTagByPath(path: String): Tag? {
        return tagDao.getTagByPath(path)?.toDomain()
    }
    
    suspend fun insertTag(tag: Tag): Long {
        return tagDao.insert(tag.toEntity())
    }
    
    suspend fun updateTag(tag: Tag) {
        tagDao.update(tag.toEntity())
    }
    
    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteById(tagId)
    }
    
    suspend fun getOrCreateTag(path: String, color: Int, icon: String? = null): Tag {
        // Check if tag exists
        val existing = getTagByPath(path)
        if (existing != null) {
            return existing
        }
        
        // Parse path components
        val components = path.split("/")
        var currentPath = ""
        var parentId: Long? = null
        
        // Create tags hierarchically
        for (component in components) {
            currentPath = if (currentPath.isEmpty()) component else "$currentPath/$component"
            
            val existingTag = getTagByPath(currentPath)
            if (existingTag != null) {
                parentId = existingTag.id
            } else {
                val newTag = Tag(
                    name = component,
                    path = currentPath,
                    parentId = parentId,
                    icon = if (currentPath == path) icon else null,
                    color = color
                )
                parentId = insertTag(newTag)
            }
        }
        
        return getTagByPath(path)!!
    }
}
