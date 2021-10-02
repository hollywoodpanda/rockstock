package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.DocumentNotFoundException
import br.org.texugo.butterbot.rockstock.MoveFileException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class RockStockService (

        val bufferSize : Int,
        private val _directory : String,
        private val tempDirectory : String

) {

    companion object {

        val LOG : Logger = LoggerFactory.getLogger(RockStockService::class.java)

        const val DIGEST_TYPE = "SHA-1"
    }

    private val directory = _directory
        get() = if (field.endsWith(File.separator)) field else "$field${File.separator}"

    @Throws(OpsyException::class)
    fun stockIt (originalDocument : Document) : Document {

        try {

            return CompletableFuture.supplyAsync {

                LOG.debug("Stocking the document ${originalDocument.canonicalPath}")

                CompressionService.compress(originalDocument, this.tempDirectory)

            }.thenApplyAsync { compressedDocument ->

                LOG.debug("Shelfilizing the document ${compressedDocument.canonicalPath} in ${this.directory}")

                ShelfService.shelfilize(compressedDocument, this.directory)

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! An error while stocking the document ${originalDocument.canonicalPath}"

            LOG.error(message, err)

            throw OpsyException(message, err)

        }

    }

    @Throws(OpsyException::class)
    fun rockIt (id : String) : Document {

        try {

            return CompletableFuture.supplyAsync {

                LOG.debug("Rocking the document $id")

                val filePath = "$directory$id${
                    CompressionService.EXTENSION_SEPARATOR
                }${
                    CompressionService.EXTENSION_COMPRESSION
                }"

                LOG.debug("Calculated path $filePath for document $id")

                val file = File(filePath)

                when (file.exists()) {

                    true -> Document(filePath)

                    false -> throw DocumentNotFoundException(id)

                }

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! Some error retrieving the document $id"

            LOG.error(message, err)

            throw OpsyException(message, err)

        }

    }

}