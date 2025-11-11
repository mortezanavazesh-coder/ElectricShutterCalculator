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
    fun insert(report: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("DELETE FROM reports")
    fun deleteAll(): Int
	
	@Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun getById(id: Long): ReportEntity?

}
