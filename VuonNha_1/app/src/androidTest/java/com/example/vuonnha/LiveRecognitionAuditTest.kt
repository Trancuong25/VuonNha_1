package com.example.vuonnha

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.data.PlantLookupRepository
import com.example.vuonnha.data.PlantScanRepository
import com.example.vuonnha.data.ScanPhoto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

/** Explicit opt-in only: calls the real provider. The orchid fixture is a user
 * screenshot, so this is an integration check, not a recognition-accuracy score. */
class LiveRecognitionAuditTest {
    @Before fun requireExplicitLiveAudit() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveAudit") == "true")
    }
    @Test fun suppliedOrchidImageTravelsThroughAppAndBackend() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fixture = File(context.getExternalFilesDir(null), "audit-orchid.png")
        assertTrue("Provide the explicitly supplied orchid screenshot", fixture.exists())
        val repo = PlantScanRepository(context)
        val photo = repo.readPhoto(Uri.fromFile(fixture))
        try {
            val candidates = repo.recognize(photo)
            assertTrue("Expected a Phalaenopsis suggestion for the supplied orchid image", candidates.any { it.scientificName.contains("Phalaenopsis", ignoreCase = true) })
            println("LIVE_ORCHID: " + candidates.joinToString { "${it.name} / ${it.scientificName} / ${it.confidence}" })
        } finally { photo.bitmap.recycle() }
    }
    @Test fun imageWithoutPlantDoesNotCreateGuessedSpecies() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        try {
            val bytes = ByteArrayOutputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); out.toByteArray() }
            val results = PlantScanRepository(context).recognize(ScanPhoto(bitmap, Base64.encodeToString(bytes, Base64.NO_WRAP)))
            assertTrue("A blank image must not invent a plant", results.isEmpty())
        } finally { bitmap.recycle() }
    }
    @Test fun nameOutsideCatalogCanBeLookedUpWithoutSavingGardenData() = runBlocking {
        val results = PlantLookupRepository().search("cây táo")
        assertTrue("Expected lookup suggestions beyond the three curated plants", results.isNotEmpty())
        assertTrue(results.all { it.fields["scientificName"].orEmpty().isNotBlank() })
        println("LIVE_LOOKUP: " + results.joinToString { it.name })
    }
}
