package com.example.vuonnha

import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.data.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class AccountBackupTest {
 @Test fun archiveRoundTripAndAutomaticSaveKeepPhotosHistoryAndAccountIsolation() = runBlocking {
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  assumeTrue(InstrumentationRegistry.getArguments().getString("accountAudit")=="true")
  val base=instrumentation.targetContext
  assumeTrue(AccountSession.user.value==null)
  val namespace="backup_test_${System.nanoTime()}"
  val directory=File(base.cacheDir,namespace).apply{mkdirs()}
  val context=object:ContextWrapper(base){
   override fun getApplicationContext()=this
   override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences("${namespace}_$name",mode)
   override fun getFilesDir()=directory
   override fun getCacheDir()=directory
  }
  val db=Room.inMemoryDatabaseBuilder(context,AppDb::class.java).build()
  val garden=CareRepository(context,db)
  val account=AccountRepository(context,"http://127.0.0.1:8788")
  val backups=AccountBackupRepository(context,"http://127.0.0.1:8788",garden)
  var observer:Job?=null
  try {
   account.authenticate(true,"Vườn thử",namespace,"garden-test-password-42")
   val photo=Bitmap.createBitmap(20,20,Bitmap.Config.ARGB_8888)
   val plant=garden.addScannedItem("Cây thử",customSpecies("Kim tiền",SpeciesGroup.PLANT,listOf("Kiểm tra đất" to 3)),listOf(3),null,emptyList(),photo,"Ảnh đầu tiên")
   photo.recycle()
   garden.done(db.dao().tasksOnce().single())
   assertNull(backups.info())
   assertEquals(1L,backups.upload(0).revision)
   garden.addItem("Cây chỉ có trên máy",customSpecies("Sen đá",SpeciesGroup.PLANT),emptyList(),null)
   assertEquals(2,db.dao().itemsOnce().size)
   backups.restore()
   assertEquals("Cây thử",db.dao().itemsOnce().single().name)
   assertEquals(1,db.dao().logsOnce().size)
   assertTrue(File(db.dao().entriesOnce().single().photoUri).exists())
   assertTrue(File(directory,"garden-before-account-restore.zip").exists())
   observer=launch {backups.observe()}
   withTimeout(20000){while((backups.info()?.revision?:0)<2)delay(300)}
   garden.addItem("Cây tự lưu",customSpecies("Trầu bà",SpeciesGroup.PLANT),emptyList(),null)
   withTimeout(20000){while((backups.info()?.revision?:0)<3)delay(300)}
   observer.cancelAndJoin();observer=null
   garden.removeItem(db.dao().itemsOnce().first{it.name=="Cây tự lưu"}.id)
   backups.restore()
   assertEquals(2,db.dao().itemsOnce().size)
   account.logout()
   account.authenticate(true,"Vườn khác",namespace+"_b","garden-test-password-42")
   assertNull(backups.info()); assertFalse(backups.enabled())
  } finally {
   observer?.cancelAndJoin()
   try { account.logout() } catch(_:Exception){};AccountSession.clear(context)
   db.close()
   check(directory.canonicalFile.parentFile==base.cacheDir.canonicalFile)
   directory.deleteRecursively()
   listOf("account_session","account_backup").forEach{base.getSharedPreferences("${namespace}_$it",0).edit().clear().commit()}
  }
 }
}
