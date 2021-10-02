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

/**
 * The main service for the RockStock operation.
 * It stocks some file in a given path as a zip on the RockStock volume.
 * It also shows the canonical path of a stored zip file, given its id
 * (the zip file name without the extension)
 *
 * @constructor Generates a new RockStockService with the given bufferSize and directories for the temporary and permanent volumes.
 */
class RockStockService (

        /** The size of the buffer for the file operations */
        val bufferSize : Int,
        /** The RockStock volume path */
        private val _directory : String,
        /** The RockStock temporary volume path */
        private val tempDirectory : String

) {

    /** Our static friend with a log and a digest type */
    companion object {

        /** The logger for the RockStockService operations */
        val LOG : Logger = LoggerFactory.getLogger(RockStockService::class.java)

        /**
         * The digest type used to calculate a hash from the file,
         * used for the stocked file's zip name
         */
        const val DIGEST_TYPE = "SHA-1"

    }

    /**
     * The directory we'll actually use on our operations.
     * It gives us a treatment to make sure our directory
     * always ends up with the {@code File.separator}
     * character
     *
     * @see File#separator
     */
    private val directory = _directory
        get() = if (field.endsWith(File.separator)) field else "$field${File.separator}"

    /**
     * The operation to stock a file located
     * at [originalDocument]'s canonical path.
     *
     * @param originalDocument The holder for the file's location to be stored in the RockStock's volume
     * @return Document The information with the stocked zip file
     *
     */
    @Throws(OpsyException::class)
    fun stockIt (originalDocument : Document) : Document {

        try {

            // We'll return the joined response from the
            // future operation
            return CompletableFuture.supplyAsync {

                LOG.debug("Stocking the document ${originalDocument.canonicalPath}")

                // Compressing the original document in the temporary directory
                CompressionService.compress(originalDocument, this.tempDirectory)

            }.thenApplyAsync { compressedDocument ->

                LOG.debug("Shelfilizing the document ${compressedDocument.canonicalPath} in ${this.directory}")

                // Shelfilizing (moving) the compressed file to the permanent directory
                // and removing the file in the temporary directory
                ShelfService.shelfilize(compressedDocument, this.directory)

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! An error while stocking the document ${originalDocument.canonicalPath}"

            LOG.error(message, err)

            // Something is wrong, can't proceed.
            throw OpsyException(message, err)

        }

    }

    /**
     * The operation to retrieve a file's path with the name [id].zip
     * located in the permanent volume. If the file exists, we return
     * the canonical path of the file in our permanent volume with a
     * 200 status code. If no file with the given name exists in our
     * permanent volume, we'll return a 404 status code.
     *
     * @param id The name of the zip file stored in our permanent volume, without the zip extension
     * @return Document The file's path in our permanent volume
     */
    @Throws(OpsyException::class)
    fun rockIt (id : String) : Document {

        try {

            // We'll return from the joined future
            return CompletableFuture.supplyAsync {

                LOG.debug("Rocking the document $id")

                // Calculating the file path with the given id
                val filePath = "$directory$id${
                    CompressionService.EXTENSION_SEPARATOR
                }${
                    CompressionService.EXTENSION_COMPRESSION
                }"

                LOG.debug("Calculated path $filePath for document $id")

                // Creating the file object with the calculated path
                val file = File(filePath)

                when (file.exists()) {

                    // If a file was found, we'll return the file's path
                    true -> Document(filePath)

                    // If no file was found, we'll throw a not found exception
                    false -> throw DocumentNotFoundException(id)

                }

            }.join()

        } catch (err : Exception) {

            val message = "Opsy! Some error retrieving the document $id"

            LOG.error(message, err)

            // Something happened that stopped us from proceeding
            // with the operation
            throw OpsyException(message, err)

        }

    }

}