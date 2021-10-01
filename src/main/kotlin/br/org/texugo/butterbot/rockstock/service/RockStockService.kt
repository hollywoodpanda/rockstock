package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.DocumentNotFoundException
import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import java.io.File
import java.util.concurrent.CompletableFuture

class RockStockService (

        val bufferSize : Int,
        private val _directory : String,
        val tempDirectory : String

) {

    companion object {
        val EXTENSION_SEPARATOR = "."
        val EXTENSION_COMPRESSION = "zip"
    }

    val directory = _directory
        get() = if (field.endsWith(File.separator)) field else "${field + File.separator}"

    private fun compress (document : Document) : Document {
        TODO("Must implement")
    }

    private fun shelfilize (tempDocument : Document) : Document {
        TODO("Must implement")
    }

    @Throws(OpsyException::class)
    fun stockIt (originalDocument : Document) : Document {

        try {

            return CompletableFuture.supplyAsync { compress(originalDocument) }.thenApplyAsync(this::shelfilize).join()

        } catch (err : Exception) {

            val message = "Opsy! An error while stocking the given document at \"${originalDocument.canonicalDirectory}\""

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