package com.example.room_employee

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert
    suspend fun insert(employeeEntity: EmployeeEntity)

    @Update
    suspend fun update(employeeEntity: EmployeeEntity)

    @Delete
    suspend fun delete(employeeEntity: EmployeeEntity)

    @Query("select * from `employee-table`")
    fun fetchAll(): Flow<List<EmployeeEntity>>

    @Query("select * from `employee-table` where id=:id")
    fun fetchEmployee(id: Int): Flow<EmployeeEntity>
}