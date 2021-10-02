package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.MoveFileException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Service responsible for storing zip files from
 * the temporary volume in the permanent volume.
 * It basically does a move file operation in the OS,
 * removing the original file in the temporary volume
 * afterwards.
 */
class ShelfService {

    companion object {

        /**
         * Moves the document in [tempDocument]'s canonical path to
         * the specified [directory]
         *
         * @param tempDocument The canonical path of the zip file in the temporary volume
         * @param directory The permanent volume path
         *
         * @return Document The canonical path of the zip file stored in the permanent volume
         */
        @Throws(OpsyException::class)
        fun shelfilize (tempDocument : Document, directory : String) : Document {

            // We'll return from this attempt to shelfilize the file.
            return try {

                RockStockService.LOG.debug("Shelfilizing temporary document ${tempDocument.canonicalPath}")

                // The document with the new canonical path, pointing to the
                // permanent volume
                val shelfilizedDocument = Document(
                        "$directory${FileService.extractDocumentFilename(tempDocument)}"
                )

                // The java.nio.file.Path of the permanent volume
                val shelfilizedDirectoryPath = Paths.get(FileService.extractDocumentDirectory(shelfilizedDocument))

                when {
                    // If the permanent volume isn't created yet, we'll create it
                    // with its necessary directory tree
                    Files.notExists(shelfilizedDirectoryPath) -> Files.createDirectories(shelfilizedDirectoryPath)
                }

                // The java.nio.file.Path of the zip file in the temporary volume
                val tempDocumentPath = Paths.get(tempDocument.canonicalPath)

                // The java.nio.file.Path of the zip file in the permanent volume
                // At this moment this path isn't pointing to anything, since the file
                // shouldn't exist yet.
                val shelfilizedDocumentPath = Paths.get(shelfilizedDocument.canonicalPath)

                when {
                    // If the file exists already, we'll remove it since it is being
                    // updated
                    Files.exists(shelfilizedDocumentPath) -> Files.delete(shelfilizedDocumentPath)
                }

                // Moving the zip file from the temporary volume to the permanent volume
                Files.move(tempDocumentPath, shelfilizedDocumentPath).let {

                    when {
                        // If the file exists in the temporary volume, we'll remove it
                        // @TODO: Is it necessary to remove a file that in theory is moved (not copied, but moved?)
                        // @TODO: This is probably garbage
                        Files.exists(tempDocumentPath) -> Files.delete(tempDocumentPath)
                    }

                    // Returning the shelfilized document if the move
                    // operation went well (there is a returned path from
                    // the move operation)
                    it?.let {

                        RockStockService.LOG.debug("Document ${shelfilizedDocument.canonicalPath} successfully shelfilized")

                        // There is a shelfilized document. We'll return it.
                        return shelfilizedDocument

                    }

                }

                RockStockService.LOG.warn("Huge probability that the document ${
                    tempDocument.canonicalPath
                } wasn't moved to ${
                    shelfilizedDocument.canonicalPath
                }")

                // Well... We didn't returned anything so something went wrong while moving
                // the file from the temporary volume to the permanent volume.
                throw MoveFileException(tempDocument.canonicalPath, shelfilizedDocument.canonicalPath)

            } catch (err : Exception) {

                val message = "Opsy! An error while \"shelfilizing\" the document ${tempDocument.canonicalPath}"

                RockStockService.LOG.error(message, err)

                // Giving up due to errors
                throw OpsyException(message, err)

            }

        }

    }

}