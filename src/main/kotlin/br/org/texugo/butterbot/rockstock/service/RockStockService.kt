package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.CompressionException
import br.org.texugo.butterbot.rockstock.DocumentNotFoundException
import br.org.texugo.butterbot.rockstock.MoveFileException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import org.slf4j.LoggerFactory
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

    val log = LoggerFactory.getLogger(RockStockService::class.java)

    companion object {
        const val DIGEST_TYPE = "SHA-1"
    }

    val directory = _directory
        get() = if (field.endsWith(File.separator)) field else "$field${File.separator}"

    @Throws(OpsyException::class)
    private fun shelfilize (tempDocument : Document) : Document {

        return try {

            log.debug("Shelfilizing temporary document ${tempDocument.canonicalPath}")

            val shelfilizedDocument = Document(
                    "$directory${FileService.extractDocumentFilename(tempDocument)}"
            )

            val shelfilizedDirectoryPath = Paths.get(FileService.extractDocumentDirectory(shelfilizedDocument))

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
                it?.let {

                    log.debug("Document ${shelfilizedDocument.canonicalPath} successfully shelfilized")

                    return shelfilizedDocument

                }

            }

            log.warn("Huge probability that the document ${
                tempDocument.canonicalPath
            } wasn't moved to ${
                shelfilizedDocument.canonicalPath
            }")

            throw MoveFileException(tempDocument.canonicalPath, shelfilizedDocument.canonicalPath)

        } catch (err : Exception) {

            val message = "Opsy! An error while \"shelfilizing\" the document ${tempDocument.canonicalPath}"

            log.error(message, err)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    fun stockIt (originalDocument : Document) : Document {

        try {

            return CompletableFuture.supplyAsync {

                log.debug("Stocking the document ${originalDocument.canonicalPath}")

                CompressionService.compress(originalDocument, this.tempDirectory)

            }.thenApplyAsync(this::shelfilize).join()

        } catch (err : Exception) {

            val message = "Opsy! An error while stocking the document ${originalDocument.canonicalPath}"

            log.error(message, err)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    fun rockIt (id : String) : Document {

        try {

            return CompletableFuture.supplyAsync {

                log.debug("Rocking the document $id")

                val filePath = "$directory$id${
                    CompressionService.EXTENSION_SEPARATOR
                }${
                    CompressionService.EXTENSION_COMPRESSION
                }"

                log.debug("Calculated path $filePath for document $id")

                val file = File(filePath)

                when (file.exists()) {

                    true -> Document(filePath)

                    false -> throw DocumentNotFoundException(id)

                }

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! Some error retrieving the document $id"

            log.error(message, err)

            throw OpsyException(message, err)

        }

    }

}