package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.MoveFileException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import java.nio.file.Files
import java.nio.file.Paths

class ShelfService {

    companion object {

        @Throws(OpsyException::class)
        fun shelfilize (tempDocument : Document, directory : String) : Document {

            return try {

                RockStockService.LOG.debug("Shelfilizing temporary document ${tempDocument.canonicalPath}")

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

                        RockStockService.LOG.debug("Document ${shelfilizedDocument.canonicalPath} successfully shelfilized")

                        return shelfilizedDocument

                    }

                }

                RockStockService.LOG.warn("Huge probability that the document ${
                    tempDocument.canonicalPath
                } wasn't moved to ${
                    shelfilizedDocument.canonicalPath
                }")

                throw MoveFileException(tempDocument.canonicalPath, shelfilizedDocument.canonicalPath)

            } catch (err : Exception) {

                val message = "Opsy! An error while \"shelfilizing\" the document ${tempDocument.canonicalPath}"

                RockStockService.LOG.error(message, err)

                throw OpsyException(message, err)

            }

        }

    }

}