package com.example.bteu_schedule.data.local.database.dao

import androidx.room.*
import com.example.bteu_schedule.data.local.database.entity.CachedFaculty
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyDao {
    
    @Query("SELECT * FROM cached_faculties ORDER BY code")
    fun getAllFaculties(): Flow<List<CachedFaculty>>
    
    @Query("SELECT * FROM cached_faculties ORDER BY code")
    suspend fun getFaculties(): List<CachedFaculty>
    
    /**
     * Получить факультеты с пагинацией для оптимизации производительности
     * @param limit Количество записей на странице
     * @param offset Смещение (номер страницы * limit)
     */
    @Query("SELECT * FROM cached_faculties ORDER BY code LIMIT :limit OFFSET :offset")
    suspend fun getFacultiesPaginated(limit: Int, offset: Int): List<CachedFaculty>
    
    @Query("SELECT * FROM cached_faculties WHERE id = :id")
    suspend fun getFacultyById(id: Int): CachedFaculty?
    
    @Query("SELECT * FROM cached_faculties WHERE code = :code")
    suspend fun getFacultyByCode(code: String): CachedFaculty?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: CachedFaculty)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculties(faculties: List<CachedFaculty>)
    
    @Update
    suspend fun updateFaculty(faculty: CachedFaculty)
    
    @Delete
    suspend fun deleteFaculty(faculty: CachedFaculty)
    
    @Query("DELETE FROM cached_faculties")
    suspend fun deleteAllFaculties()
    
    @Query("SELECT COUNT(*) FROM cached_faculties")
    suspend fun getFacultiesCount(): Int
}

