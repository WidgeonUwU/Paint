package ru.sfedu.paint.util

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object JsonCompression {
    fun compress(json: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        val deflaterStream = DeflaterOutputStream(outputStream, deflater)
        
        deflaterStream.write(json.toByteArray(Charsets.UTF_8))
        deflaterStream.finish()
        deflaterStream.close()
        
        return outputStream.toByteArray()
    }
    
    fun decompress(compressed: ByteArray): String {
        val inputStream = java.io.ByteArrayInputStream(compressed)
        val inflater = Inflater()
        val inflaterStream = InflaterInputStream(inputStream, inflater)
        
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        
        var bytesRead: Int
        while (inflaterStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        
        inflaterStream.close()
        
        return outputStream.toString(Charsets.UTF_8.name())
    }
}
