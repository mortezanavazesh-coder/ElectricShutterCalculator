package com.morteza.shuttercalculator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun allReportsFlow(): Flow<List<ReportEntity>>

    @Insert
    suspend fun insert(report: ReportEntity): Long

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReportEntity?

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM reports")
    suspend fun deleteAll(): Int
}
