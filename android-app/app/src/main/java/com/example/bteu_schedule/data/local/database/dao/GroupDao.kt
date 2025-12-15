package com.example.bteu_schedule.data.local.database.dao

import androidx.room.*
import com.example.bteu_schedule.data.local.database.entity.CachedGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    @Query("SELECT * FROM cached_groups ORDER BY code")
    fun getAllGroups(): Flow<List<CachedGroup>>
    
    @Query("SELECT * FROM cached_groups ORDER BY code")
    suspend fun getAllGroupsList(): List<CachedGroup>
    
    /**
     * Получить группы с пагинацией для оптимизации производительности
     * @param limit Количество записей на странице
     * @param offset Смещение (номер страницы * limit)
     */
    @Query("SELECT * FROM cached_groups ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getGroupsPaginated(limit: Int, offset: Int): List<CachedGroup>
    
    /**
     * Получить общее количество групп
     */
    @Query("SELECT COUNT(*) FROM cached_groups")
    suspend fun getGroupsCount(): Int
    
    @Query("SELECT * FROM cached_groups WHERE code = :code LIMIT 1")
    suspend fun getGroupByCode(code: String): CachedGroup?
    
    @Query("SELECT * FROM cached_groups WHERE code = :code AND facultyCode = :facultyCode")
    suspend fun getGroupByCodeAndFaculty(code: String, facultyCode: String): CachedGroup?
    
    @Query("SELECT * FROM cached_groups WHERE facultyCode = :facultyCode AND educationForm = :educationForm")
    suspend fun getGroupsByFacultyAndForm(facultyCode: String, educationForm: String): List<CachedGroup>
    
    @Query("SELECT * FROM cached_groups WHERE facultyCode = :facultyCode AND educationForm = :educationForm AND course = :course")
    suspend fun getGroupsByFacultyFormAndCourse(
        facultyCode: String,
        educationForm: String,
        course: Int
    ): List<CachedGroup>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: CachedGroup)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<CachedGroup>)
    
    @Update
    suspend fun updateGroup(group: CachedGroup)
    
    @Delete
    suspend fun deleteGroup(group: CachedGroup)
    
    @Query("DELETE FROM cached_groups")
    suspend fun deleteAllGroups()
    
    @Query("DELETE FROM cached_groups WHERE facultyCode = :facultyCode")
    suspend fun deleteGroupsByFaculty(facultyCode: String)
}

