package com.example.vuonnha.data

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupLimitsTest {
    @Test fun exactLimitIsAccepted() {
        val data = ByteArray(8193) { 42 }
        assertArrayEquals(data, ByteArrayInputStream(data).readBackupBytes(data.size.toLong()))
    }
    @Test fun rejectsDecompressedDataEvenWhenArchiveIsSmall() {
        val archive = ByteArrayOutputStream().apply {
            ZipOutputStream(this).use { it.putNextEntry(ZipEntry("photos/a.jpg")); it.write(ByteArray(100_000)); it.closeEntry() }
        }.toByteArray()
        assertTrue(archive.size < 10_000)
        ZipInputStream(ByteArrayInputStream(archive)).use {
            it.nextEntry
            try { it.readBackupBytes(10_000); fail("Oversized decompressed data must be rejected") }
            catch (expected: IllegalArgumentException) { assertTrue(expected.message!!.contains("giới hạn")) }
        }
    }
    @Test fun overLimitChunkIsNotWritten() {
        val out = ByteArrayOutputStream()
        try { ByteArrayInputStream(ByteArray(9000)).copyBackupTo(out, 8500); fail() }
        catch (expected: IllegalArgumentException) { assertEquals(8192, out.size()) }
    }
}
