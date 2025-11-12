package com.morteza.shuttercalculator

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReportEntity?

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReportEntity>

    @Insert
    suspend fun insert(report: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM reports")
    suspend fun deleteAll(): Int
}
