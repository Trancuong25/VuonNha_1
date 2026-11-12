package com.example.vuonnha.data

import java.io.InputStream
import java.io.OutputStream

/** Count decompressed bytes rather than trusting archive metadata. */
internal fun InputStream.copyBackupTo(out: OutputStream, limit: Long): Long {
    require(limit >= 0)
    var total = 0L
    val buffer = ByteArray(8192)
    while (true) {
        val count = read(buffer)
        if (count < 0) return total
        require(total + count <= limit) { "File sao lưu vượt giới hạn cho phép." }
        out.write(buffer, 0, count)
        total += count
    }
}

internal fun InputStream.readBackupBytes(limit: Long): ByteArray = java.io.ByteArrayOutputStream().use {
    copyBackupTo(it, limit)
    it.toByteArray()
}
