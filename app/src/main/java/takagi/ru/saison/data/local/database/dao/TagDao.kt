package takagi.ru.saison.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.saison.data.local.database.entities.TagEntity

@Dao
interface TagDao {
    
    @Query("SELECT * FROM tags ORDER BY path ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags")
    suspend fun getAllTagsList(): List<TagEntity>
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity?
    
    @Query("SELECT * FROM tags WHERE path = :path")
    suspend fun getTagByPath(path: String): TagEntity?
    
    @Query("SELECT * FROM tags WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildTags(parentId: Long): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootTags(): Flow<List<TagEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>): List<Long>
    
    @Update
    suspend fun update(tag: TagEntity)
    
    @Delete
    suspend fun delete(tag: TagEntity)
    
    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteById(tagId: Long)
}
