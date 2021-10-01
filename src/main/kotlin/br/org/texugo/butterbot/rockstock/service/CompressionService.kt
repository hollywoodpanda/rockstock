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

class CompressionService {

    companion object {

        val LOG = LoggerFactory.getLogger(CompressionService::class.java)

        val EXTENSION_SEPARATOR = "."
        val EXTENSION_COMPRESSION = "zip"

        @Throws(OpsyException::class)
        private fun generateTempZip (id : String, tempDirectory : String) : File {

            return try {

                LOG.debug("Generating temporary zip file for $id at $tempDirectory")

                val tempDirectoryPath = Paths.get(tempDirectory)

                if (Files.notExists(tempDirectoryPath))
                    Files.createDirectories(tempDirectoryPath)

                val tempZipPath = Paths.get(
                        tempDirectory,
                        "$id${EXTENSION_SEPARATOR}${EXTENSION_COMPRESSION}"
                )

                if (Files.exists(tempZipPath))
                    Files.delete(tempZipPath)

                LOG.debug("Temporary zip file created for $id")

                Files.createFile(tempZipPath).toFile()

            } catch (err : Exception) {

                val message = "Opsy! Something went wrong while creating the temporary zip file: ${err.message}"

                LOG.error(message, err)

                throw OpsyException(message, err)

            }

        }

        fun compress (document : Document, temporaryDirectory : String,  bufferSize : Int = 1024) : Document {

            return try {

                LOG.debug("Compressing file ${document.canonicalPath} and saving temporarily at $temporaryDirectory")

                val documentId = HashGenerationService.generateHash(document)

                val temporaryZipFile = generateTempZip(documentId, temporaryDirectory)

                val newDocument = Document(temporaryZipFile.canonicalPath)

                val fos = FileOutputStream(newDocument.canonicalPath)

                val zipOut = ZipOutputStream(fos)

                val fileToZip = File(document.canonicalPath)

                val fis = FileInputStream(fileToZip)

                val zipEntry = ZipEntry(FileService.extractDocumentFilename(document))

                zipOut.putNextEntry(zipEntry)

                val bufferBytes = ByteArray(bufferSize)

                var length: Int

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

                LOG.debug("File ${newDocument.canonicalPath} successfully compressed")

                // Returning the newDocument
                newDocument

            } catch (err : Exception) {

                val message = "Opsy! Something went wrong while compressing ${document.canonicalPath}"

                LOG.error(message, err)

                throw CompressionException(message)

            }

        }

    }

}