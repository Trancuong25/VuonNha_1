package com.example.vuonnha.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface CareDao {
    @Query("SELECT * FROM Item ORDER BY id DESC") fun items(): Flow<List<Item>>
    @Query("SELECT * FROM Item") suspend fun itemsOnce(): List<Item>
    @Query("SELECT * FROM Item WHERE id=:id") suspend fun itemOnce(id: Long): Item?
    @Insert suspend fun insertItem(i: Item): Long
    @Update suspend fun updateItem(i: Item)
    @Query("DELETE FROM Item WHERE id=:id") suspend fun deleteItem(id: Long)
    @Query("DELETE FROM Item") suspend fun clearItems()

    @Query("SELECT * FROM Zone ORDER BY id") fun zones(): Flow<List<Zone>>
    @Query("SELECT * FROM Zone") suspend fun zonesOnce(): List<Zone>
    @Insert suspend fun insertZone(z: Zone): Long
    @Query("DELETE FROM Zone WHERE id=:id") suspend fun deleteZone(id: Long)
    @Query("DELETE FROM Zone") suspend fun clearZones()
    @Query("UPDATE Item SET zoneId=NULL WHERE zoneId=:id") suspend fun clearZoneOf(id: Long)

    @Query("SELECT * FROM CareTask") fun tasks(): Flow<List<CareTask>>
    @Query("SELECT * FROM CareTask") suspend fun tasksOnce(): List<CareTask>
    @Query("SELECT * FROM CareTask WHERE id=:id") suspend fun taskOnce(id: Long): CareTask?
    @Query("UPDATE CareTask SET everyDays=:days WHERE id=:id") suspend fun updateCycle(id: Long, days: Int)
    @Insert suspend fun insertTasks(t: List<CareTask>)
    @Insert suspend fun insertTask(t: CareTask): Long
    @Update suspend fun updateTask(t: CareTask)
    @Delete suspend fun deleteTask(t: CareTask)

    @Query("SELECT * FROM CareLog") fun logs(): Flow<List<CareLog>>
    @Query("SELECT * FROM CareLog") suspend fun logsOnce(): List<CareLog>
    @Insert suspend fun insertLog(l: CareLog)

    @Query("SELECT * FROM DiaryEntry ORDER BY at DESC") fun allEntries(): Flow<List<DiaryEntry>>
    @Query("SELECT * FROM DiaryEntry") suspend fun entriesOnce(): List<DiaryEntry>
    @Query("SELECT * FROM DiaryEntry WHERE itemId=:id") suspend fun entriesOf(id: Long): List<DiaryEntry>
    @Insert suspend fun insertEntry(e: DiaryEntry)
    @Delete suspend fun deleteEntry(e: DiaryEntry)
}
