package com.example.vuonnha.data

import androidx.room.*

// DATABASE v2: khoá ngoại (xoá cây thì tự xoá việc, nhật ký) + index cho cột itemId.
@Entity
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val speciesId: String,
    val zoneId: Long? = null, // khu vực (null = chưa phân khu)
    @ColumnInfo(defaultValue = "''") val profileJson: String = ""
)

// DATABASE v3: khu vực trong nhà. outdoor = ngoài trời (chịu ảnh hưởng thời tiết), covered = có mái che.
@Entity
data class Zone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String,
    val outdoor: Boolean, val covered: Boolean = false, val emoji: String
)

@Entity(
    foreignKeys = [ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("itemId")]
)
data class CareTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val itemId: Long, val name: String,
    val everyDays: Int, val lastDone: Long, val snoozeUntil: Long = 0,
    // DATABASE v4: loại việc (xem TaskType). Tên enum, mặc định OTHER.
    @ColumnInfo(defaultValue = "OTHER") val type: String = "OTHER"
)

@Entity(
    foreignKeys = [ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("itemId")]
)
data class CareLog(@PrimaryKey(autoGenerate = true) val id: Long = 0, val itemId: Long, val task: String, val at: Long)

@Entity(
    foreignKeys = [ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("itemId")]
)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val itemId: Long, val at: Long,
    val note: String = "", val photoUri: String = ""
)
