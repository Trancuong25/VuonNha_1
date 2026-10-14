package com.example.vuonnha.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Item::class, CareTask::class, CareLog::class, DiaryEntry::class, Zone::class], version = 5, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): CareDao

    companion object {
        @Volatile private var inst: AppDb? = null

        // Migration 1 -> 2: dựng lại 3 bảng con có khoá ngoại + index, giữ nguyên dữ liệu cũ.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                rebuild(db, "CareTask", "`id`,`itemId`,`name`,`everyDays`,`lastDone`,`snoozeUntil`",
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER NOT NULL, `name` TEXT NOT NULL, `everyDays` INTEGER NOT NULL, `lastDone` INTEGER NOT NULL, `snoozeUntil` INTEGER NOT NULL")
                rebuild(db, "CareLog", "`id`,`itemId`,`task`,`at`",
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER NOT NULL, `task` TEXT NOT NULL, `at` INTEGER NOT NULL")
                rebuild(db, "DiaryEntry", "`id`,`itemId`,`at`,`note`,`photoUri`",
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER NOT NULL, `at` INTEGER NOT NULL, `note` TEXT NOT NULL, `photoUri` TEXT NOT NULL")
            }
        }

        // Migration 2 -> 3: thêm bảng Zone và cột Item.zoneId (cây cũ để null = chưa phân khu). Không mất dữ liệu.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `Zone` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `outdoor` INTEGER NOT NULL, `covered` INTEGER NOT NULL, `emoji` TEXT NOT NULL)")
                db.execSQL("ALTER TABLE `Item` ADD COLUMN `zoneId` INTEGER")
            }
        }

        // Migration 3 -> 4: thêm CareTask.type và gán loại cho các việc cũ dựa trên tên. Không mất dữ liệu.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `CareTask` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'OTHER'")
                db.execSQL(
                    "UPDATE `CareTask` SET `type` = CASE " +
                        "WHEN `name` LIKE '%phân%' THEN 'FERTILIZE' " +
                        "WHEN `name` LIKE '%thay nước%' THEN 'WATER_CHANGE' " +
                        "WHEN `name` LIKE '%tưới%' THEN 'WATER' " +
                        "WHEN `name` LIKE '%cho ăn%' OR `name` LIKE '%thức ăn%' THEN 'FEED' " +
                        "WHEN `name` LIKE '%vệ sinh%' OR `name` LIKE '%lọc%' THEN 'CLEAN' " +
                        "WHEN `name` LIKE '%lót%' THEN 'BEDDING' " +
                        "WHEN `name` LIKE '%tắm%' THEN 'BATH' " +
                        "ELSE 'OTHER' END"
                )
            }
        }

        private fun rebuild(db: SupportSQLiteDatabase, table: String, cols: String, ddl: String) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `${table}_new` ($ddl, FOREIGN KEY(`itemId`) REFERENCES `Item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("INSERT INTO `${table}_new` ($cols) SELECT $cols FROM `$table` WHERE `itemId` IN (SELECT `id` FROM `Item`)")
            db.execSQL("DROP TABLE `$table`")
            db.execSQL("ALTER TABLE `${table}_new` RENAME TO `$table`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_${table}_itemId` ON `$table` (`itemId`)")
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `Item` ADD COLUMN `profileJson` TEXT NOT NULL DEFAULT ''")
            }
        }
        fun get(c: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDb::class.java, "vuonnha.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { inst = it }
        }
    }
}
