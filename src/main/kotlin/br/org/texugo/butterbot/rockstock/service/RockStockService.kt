package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.CompressionException
import br.org.texugo.butterbot.rockstock.DocumentNotFoundException
import br.org.texugo.butterbot.rockstock.MoveFileException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.experimental.and

class RockStockService (

        val bufferSize : Int,
        private val _directory : String,
        val tempDirectory : String

) {

    companion object {
        val EXTENSION_SEPARATOR = "."
        val EXTENSION_COMPRESSION = "zip"
        val DIGEST_TYPE = "SHA-1"
    }

    val directory = _directory
        get() = if (field.endsWith(File.separator)) field else "${field + File.separator}"

    @Throws(OpsyException::class)
    private fun generateHash (document : Document) : String {

        return try {

            Files.newInputStream(Paths.get(document.canonicalPath)).use {

                // @TODO use a specific value for the hash buffer size
                val buffer = ByteArray(bufferSize)

                val messageDigest = MessageDigest.getInstance(DIGEST_TYPE)

                var read = -1

                do {

                    read = it.read(buffer)

                    when {
                        read >= 0 -> messageDigest.update(buffer, 0, read)
                    }

                } while (read >= 0)

                val textBuilder = StringBuilder()

                val hash = messageDigest.digest()

                for (i in 0 .. hash.size) {

                    textBuilder.append(
                            ((hash[i] and 0xff.toByte()) + 0x100).toString(16).substring(1)
                    )

                }

                // Returning the hash!
                textBuilder.toString()

            }

        } catch (err : Exception) {

            val message = "Opsy! Error generating hash for \"${document.canonicalPath}\""

            println(message)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    private fun generateTempZip (id : String) : File {

        return try {

            val tempDirectoryPath = Paths.get(this.tempDirectory)

            if (Files.notExists(tempDirectoryPath))
                Files.createDirectories(tempDirectoryPath)

            val tempZipPath = Paths.get(this.tempDirectory, "$id$EXTENSION_SEPARATOR$EXTENSION_COMPRESSION")

            if (Files.exists(tempZipPath))
                Files.delete(tempZipPath)

            Files.createFile(tempZipPath).toFile()

        } catch (err : Exception) {
            val message = "Opsy! Something went wrong while creating the temporary zip file: ${err.message}"
            println(message)
            throw OpsyException(message, err)
        }

    }

    private fun extractDocumentDirectory (document : Document) : String
        = document.canonicalPath.substring(0, document.canonicalPath.lastIndexOf(File.separator))

    private fun extractDocumentFilename (document : Document) : String
        = document.canonicalPath.substring(document.canonicalPath.lastIndexOf(File.separator) + 1)

    private fun compress (document : Document) : Document {

        return try {

            val documentId = generateHash(document)

            val temporaryZipFile = generateTempZip(documentId)

            val newDocument = Document(temporaryZipFile.canonicalPath)

            val fos = FileOutputStream(newDocument.canonicalPath)

            val zipOut = ZipOutputStream(fos)

            val fileToZip = File(document.canonicalPath)

            val fis = FileInputStream(fileToZip)

            val zipEntry = ZipEntry(extractDocumentFilename(document))

            zipOut.putNextEntry(zipEntry)

            val bufferBytes = ByteArray(bufferSize)

            var length = -1

            do {

                length = fis.read(bufferBytes)

                when {

                    length >= 0 -> {

                        zipOut.write(bufferBytes, 0, length)

                    }

                }

            } while (length >= 0)

            zipOut.close()

            fis.close()

            fos.close()

            println("[DEBUG] Compressed file \"${newDocument.canonicalPath}\" created!")

            // Returning the newDocument
            newDocument

        } catch (err : Exception) {
            val message = "Opsy! Something went wrong while compressing \"${document.canonicalPath}\""
            println(message)
            throw CompressionException(message)
        }

    }

    @Throws(OpsyException::class)
    private fun shelfilize (tempDocument : Document) : Document {

        return try {

            val shelfilizedDocument = Document("$directory${extractDocumentFilename(tempDocument)}")

            val shelfilizedDirectoryPath = Paths.get(extractDocumentDirectory(shelfilizedDocument))

            when {
                Files.notExists(shelfilizedDirectoryPath) -> Files.createDirectories(shelfilizedDirectoryPath)
            }

            val tempDocumentPath = Paths.get(tempDocument.canonicalPath)
            val shelfilizedDocumentPath = Paths.get(shelfilizedDocument.canonicalPath)

            when {
                Files.exists(shelfilizedDocumentPath) -> Files.delete(shelfilizedDocumentPath)
            }

            Files.move(tempDocumentPath, shelfilizedDocumentPath).let {

                when {
                    Files.exists(tempDocumentPath) -> Files.delete(tempDocumentPath)
                }

                // Returning the shelfilized document if the move
                // operation went well
                it?.let { shelfilizedDocument }

            }

            throw MoveFileException(tempDocument.canonicalPath, shelfilizedDocument.canonicalPath)

        } catch (err : Exception) {

            val message = "Opsy! An error while \"shelfilizing\" the given document \"${tempDocument.canonicalPath}\""

            println(message)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    fun stockIt (originalDocument : Document) : Document {

        try {

            return CompletableFuture.supplyAsync { compress(originalDocument) }.thenApplyAsync(this::shelfilize).join()

        } catch (err : Exception) {

            val message = "Opsy! An error while stocking the given document \"${originalDocument.canonicalPath}\""

            println(message)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    fun rockIt (id : String) : Document {

        try {

            return CompletableFuture.supplyAsync {

                println("[DEBUG] Inside the rockIt's future")

                val filePath = "$directory$id$EXTENSION_SEPARATOR$EXTENSION_COMPRESSION"

                val file = File(filePath)

                when (file.exists()) {

                    true -> Document(filePath)

                    false -> throw DocumentNotFoundException(id)

                }

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! Some error retrieving the \"$id\" document"

            println(message)

            throw OpsyException(message, err)

        }

    }

}