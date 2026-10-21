package com.example.vuonnha

import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupTransactionTest {
    @Test fun editingProfileAndCyclesIsAtomic() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDb::class.java).build()
        try {
            val repo = CareRepository(context, db)
            val first = repo.addItem("Cây gốc", customSpecies("Kim tiền", SpeciesGroup.PLANT, listOf("Tưới nước" to 7)), listOf(7), null)
            val other = repo.addItem("Cây khác", customSpecies("Trầu bà", SpeciesGroup.PLANT, listOf("Tưới nước" to 3)), listOf(3), null)
            val tasks = db.dao().tasksOnce()
            val ownTask = tasks.first { it.itemId == first }; val otherTask = tasks.first { it.itemId == other }
            val original = requireNotNull(db.dao().itemOnce(first))
            try {
                repo.saveItemEdit(first, "Tên mới", null, original.speciesId, "{\"location\":\"Cửa sổ\"}", linkedMapOf(ownTask.id to 5, otherTask.id to 6))
                fail("A task belonging to another plant must be rejected")
            } catch (expected: IllegalArgumentException) { }
            assertEquals("Cây gốc", db.dao().itemOnce(first)?.name)
            assertEquals(7, db.dao().taskOnce(ownTask.id)?.everyDays)
            assertEquals(3, db.dao().taskOnce(otherTask.id)?.everyDays)
            repo.saveItemEdit(first, "Tên mới", null, original.speciesId, "{\"location\":\"Cửa sổ\"}", mapOf(ownTask.id to 5))
            assertEquals("Tên mới", db.dao().itemOnce(first)?.name)
            assertEquals(5, db.dao().taskOnce(ownTask.id)?.everyDays)
            assertTrue(requireNotNull(db.dao().itemOnce(first)).profileJson.contains("Cửa sổ"))
        } finally { db.close() }
    }
    @Test fun plantProfileSurvivesSaveAndBackupRestore() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(base, AppDb::class.java).build()
        try {
            val repo = CareRepository(base, db)
            val profile = """{"scientificName":"Phalaenopsis spp.","actualHeight":"35","humidity":"50–80%"}"""
            repo.addItem("Lan bàn học", customSpecies("Lan hồ điệp", SpeciesGroup.PLANT).copy(profileJson = profile), emptyList(), null)
            assertEquals(profile, db.dao().itemsOnce().single().profileJson)
            val archive = ByteArrayOutputStream()
            repo.exportZip(archive)
            repo.importBackup(ByteArrayInputStream(archive.toByteArray()))
            assertEquals("Lan bàn học", db.dao().itemsOnce().single().name)
            assertEquals(profile, db.dao().itemsOnce().single().profileJson)
        } finally { db.close() }
    }
    @Test fun failedImportRollsBackDatabaseAndCopiedPhotos() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File.createTempFile("backup_test_", ".tmp", base.cacheDir).apply { delete(); mkdir() }
        val context = object : ContextWrapper(base) { override fun getFilesDir() = folder }
        val db = Room.inMemoryDatabaseBuilder(base, AppDb::class.java).build()
        try {
            val original = db.dao().insertItem(Item(name = "Cây cũ", speciesId = "unknown"))
            val json = """{"items":[{"id":1,"name":"Cây mới","speciesId":"unknown"}],"diary":[{"itemId":1,"at":1,"photo":"photos/a.jpg"},{"itemId":1,"photo":"photos/a.jpg"}]}"""
            val bytes = ByteArrayOutputStream().apply {
                ZipOutputStream(this).use {
                    it.putNextEntry(ZipEntry("data.json")); it.write(json.toByteArray()); it.closeEntry()
                    it.putNextEntry(ZipEntry("photos/a.jpg")); it.write(byteArrayOf(1,2,3)); it.closeEntry()
                }
            }.toByteArray()
            try { CareRepository(context, db).importBackup(ByteArrayInputStream(bytes)); fail("Malformed diary must fail") }
            catch (expected: org.json.JSONException) { }
            assertEquals(original, db.dao().itemsOnce().single().id)
            assertEquals("Cây cũ", db.dao().itemsOnce().single().name)
            assertTrue(folder.listFiles().orEmpty().isEmpty())
        } finally { db.close(); folder.deleteRecursively() }
    }

    @Test fun invalidScannedPlantLeavesNoImageOrItem() = runBlocking {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File.createTempFile("scan_test_", ".tmp", base.cacheDir).apply { delete(); mkdir() }
        val context = object : ContextWrapper(base) { override fun getFilesDir() = folder }
        val db = Room.inMemoryDatabaseBuilder(base, AppDb::class.java).build()
        val photo = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            try {
                CareRepository(context, db).addScannedItem("", customSpecies("Cây thử", SpeciesGroup.PLANT), emptyList(), null, emptyList(), photo, "")
                fail("Empty plant name must fail")
            } catch (expected: IllegalArgumentException) { }
            assertTrue(db.dao().itemsOnce().isEmpty())
            assertTrue(folder.listFiles().orEmpty().isEmpty())
        } finally { photo.recycle(); db.close(); folder.deleteRecursively() }
    }
}
