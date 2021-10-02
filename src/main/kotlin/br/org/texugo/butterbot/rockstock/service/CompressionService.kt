package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.CompressionException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * The file compressor.
 * It compress a file in the zip format and stores it
 * in the RockStock temporary volume.
 */
class CompressionService {

    companion object {

        /** The file compressor logger */
        val LOG = LoggerFactory.getLogger(CompressionService::class.java)

        /** The symbol used to separate the file's name from the file's extension */
        val EXTENSION_SEPARATOR = "."

        /** The file extension for zip files */
        val EXTENSION_COMPRESSION = "zip"

        /**
         * Generate an empty zip file in the temporary volume specified in [tempDirectory]
         * with the name [id].zip
         *
         * @param id The identification of the file. Used as the zip file name
         * @param tempDirectory the temporary volume to store the created zip file
         *
         * @return File the created empty zip [File]
         */
        @Throws(OpsyException::class)
        private fun generateTempZip (id : String, tempDirectory : String) : File {

            // We'll return whatever comes from this attempt (if anything comes from it)
            return try {

                LOG.debug("Generating temporary zip file for $id at $tempDirectory")

                // The java.nio.file.Path of the temporary volume
                val tempDirectoryPath = Paths.get(tempDirectory)

                // If the temporary volume doesn't exist yet,
                // we'll create it (and the necessary directory structure).
                if (Files.notExists(tempDirectoryPath))
                    Files.createDirectories(tempDirectoryPath)

                // The java.nio.file.Path for the zip file
                // that we'll create
                val tempZipPath = Paths.get(
                        tempDirectory,
                        "$id${EXTENSION_SEPARATOR}${EXTENSION_COMPRESSION}"
                )

                // If the zip file already exists we'll remove it
                // (it is going to be replaced)
                if (Files.exists(tempZipPath))
                    Files.delete(tempZipPath)

                LOG.debug("Temporary zip file created for $id")

                // We create the zip file and convert it to a "java" File
                Files.createFile(tempZipPath).toFile()

            } catch (err : Exception) {

                val message = "Opsy! Something went wrong while creating the temporary zip file: ${err.message}"

                LOG.error(message, err)

                // I don't understand what happened here! OMG!
                throw OpsyException(message, err)

            }

        }

        /**
         * Compress the file specified in the [document]'s canonical path to a zip and stores it
         * in the specified [temporaryDirectory]
         *
         * @param document The document with the uncompressed file's canonical path
         * @param temporaryDirectory The temporary volume where the file will be stored
         * @param bufferSize The size of the buffer for the compression process. It defaults to 1024
         *
         * @return Document the created zip file information (the canonical path of the created zip)
         *
         */
        fun compress (document : Document, temporaryDirectory : String,  bufferSize : Int = 1024) : Document {

            // Returning whatever comes out of this soup
            return try {

                LOG.debug("Compressing file ${document.canonicalPath} and saving temporarily at $temporaryDirectory")

                // Calculating the hash from the document's canonical path
                val documentId = HashGenerationService.generateHash(document)

                // Creating the empty zip file
                val temporaryZipFile = generateTempZip(documentId, temporaryDirectory)

                // Creating the new document with the created zip file's canonical path
                val newDocument = Document(temporaryZipFile.canonicalPath)

                // The file's output stream we'll write stuff to
                val fos = FileOutputStream(newDocument.canonicalPath)

                // Encapsulating the output stream to a zip output stream (cool delegate)
                val zipOut = ZipOutputStream(fos)

                // Creating the file object for the original uncompressed file
                val fileToZip = File(document.canonicalPath)

                // The file input stream we'll read stuff from
                val fis = FileInputStream(fileToZip)

                // Creating the file entry for the zip file
                val zipEntry = ZipEntry(FileService.extractDocumentFilename(document))

                // Putting the file info in the zip
                zipOut.putNextEntry(zipEntry)

                // Our buffer to write the zip file
                val bufferBytes = ByteArray(bufferSize)

                // The readed byte's length from the input stream
                var length: Int

                do {

                    // We read the file in the buffer. It gives us the size of
                    // what was readed (at least I think it does that - too lazy to confirm)
                    length = fis.read(bufferBytes)

                    when {

                        // If something was readed
                        length >= 0 -> {

                            // We write the readed information stored in the buffer to the zip
                            zipOut.write(bufferBytes, 0, length)

                        }

                    }

                } while (length >= 0) // When there is nothing else to read in the file, length will be -1

                // Closing the output stream for the zip
                zipOut.close()

                // Closing the input stream from the original uncompressed file
                fis.close()

                // Closing the output stream of the generated zip (is it really what's happening? Lots of doubts here)
                fos.close()

                LOG.debug("File ${newDocument.canonicalPath} successfully compressed")

                // Returning the newDocument
                newDocument

            } catch (err : Exception) {

                val message = "Opsy! Something went wrong while compressing ${document.canonicalPath}"

                LOG.error(message, err)

                // Let us pray for a better future with a success flow
                throw CompressionException(message)

            }

        }

    }

}