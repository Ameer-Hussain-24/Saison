package takagi.ru.saison.data.local.encryption

import android.util.Base64
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    private val keystoreHelper: KeystoreHelper
) {
    
    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
    
    fun encrypt(data: String): EncryptedData {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreHelper.getMasterKey())
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return EncryptedData(
            ciphertext = Base64.encodeToString(encrypted, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }
    
    fun decrypt(encryptedData: EncryptedData): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, keystoreHelper.getMasterKey(), spec)
        
        val ciphertext = Base64.decode(encryptedData.ciphertext, Base64.NO_WRAP)
        val decrypted = cipher.doFinal(ciphertext)
        
        return String(decrypted, Charsets.UTF_8)
    }
    
    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreHelper.getMasterKey())
        
        // Write IV to file header
        outputFile.outputStream().use { output ->
            output.write(cipher.iv)
            
            inputFile.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) {
                        output.write(encrypted)
                    }
                }
                
                val final = cipher.doFinal()
                if (final != null) {
                    output.write(final)
                }
            }
        }
    }
    
    fun decryptFile(inputFile: File, outputFile: File) {
        inputFile.inputStream().use { input ->
            // Read IV from file header
            val iv = ByteArray(12) // GCM standard IV size
            input.read(iv)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, keystoreHelper.getMasterKey(), spec)
            
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val decrypted = cipher.update(buffer, 0, bytesRead)
                    if (decrypted != null) {
                        output.write(decrypted)
                    }
                }
                
                val final = cipher.doFinal()
                if (final != null) {
                    output.write(final)
                }
            }
        }
    }
    
    fun encryptBytes(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreHelper.getMasterKey())
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        
        return Pair(encrypted, iv)
    }
    
    fun decryptBytes(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, keystoreHelper.getMasterKey(), spec)
        return cipher.doFinal(encryptedData)
    }
}
