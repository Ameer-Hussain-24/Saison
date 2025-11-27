package takagi.ru.saison.util.backup

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow

class BackupFileManager @Inject constructor() {
    
    /**
     * 创建 ZIP 压缩文件
     * @param files Map of filename to file content (JSON string)
     * @param outputFile Output ZIP file
     */
    fun createZipArchive(files: Map<String, String>, outputFile: File) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            files.forEach { (filename, content) ->
                val entry = ZipEntry(filename)
                zipOut.putNextEntry(entry)
                zipOut.write(content.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
        }
    }
    
    /**
     * 解压 ZIP 文件
     * @param zipFile ZIP file to extract
     * @return Map of filename to file content (JSON string)
     */
    fun extractZipArchive(zipFile: File): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val content = zipIn.readBytes().toString(Charsets.UTF_8)
                    result[entry.name] = content
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        
        return result
    }
    
    /**
     * 格式化文件大小为人类可读格式
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        
        val size = bytes / 1024.0.pow(digitGroups.toDouble())
        return String.format("%.1f %s", size, units[digitGroups])
    }
}
