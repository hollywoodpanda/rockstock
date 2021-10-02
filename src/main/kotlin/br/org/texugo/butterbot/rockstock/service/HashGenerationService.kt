package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.experimental.and

/**
 * Generates [String] hashes from a given [Document.canonicalPath]
 */
class HashGenerationService {

    companion object {

        /** The hash service logger */
        val LOG = LoggerFactory.getLogger(HashGenerationService::class.java)

        /**
         * The fixed buffer size for the digest stuff
         * @TODO Should we care about this size for our given purpose?
         */
        val BUFFER_SIZE = 1024

        /**
         * Generates a hash [String] from the given [Document.canonicalPath]
         *
         * @param document The document with the canonical path to be converted to a hash String
         * @return String The hash from the document's canonical path
         */
        @Throws(OpsyException::class)
        fun generateHash (document : Document) : String {

            LOG.debug("Generating hash for document ${document.canonicalPath}")

            // We'll return from this attempt to generate a hash
            return try {

                // Creating an input stream with sophistication
                Files.newInputStream(Paths.get(document.canonicalPath)).use {

                    // Creating our byte array buffer with our fixed buffer size
                    val buffer = ByteArray(BUFFER_SIZE)

                    // The digest mythical object
                    val messageDigest = MessageDigest.getInstance(RockStockService.DIGEST_TYPE)

                    // The readed counter from the input stream.
                    var read: Int

                    do {

                        // We read it and store the readed bytes in our buffer.
                        read = it.read(buffer)

                        when {
                            // If something was readed, we'll update the mythical digest
                            // with the new information
                            read >= 0 -> messageDigest.update(buffer, 0, read)
                        }

                    } while (read >= 0) // Stop if the read flag turns smaller than 0

                    // We'll append our hash in this builder while
                    // it is being calculated
                    val textBuilder = StringBuilder()

                    // The mythical digested hash
                    val hash = messageDigest.digest()

                    // For every byte in the digested mythical hash
                    for (hashBit in hash) {

                        // We'll append it to the string builder,
                        // with a lot of magical stuff from stackoverflow
                        textBuilder.append(
                                ((hashBit and 0xff.toByte()) + 0x100).toString(16).substring(1)
                        )

                    }

                    // Getting the actual string from the builder
                    val hashValue = textBuilder.toString()

                    // Printing the string so the logs can know it
                    LOG.debug("Hash $hashValue generated for ${document.canonicalPath}")

                    // Returning the hash!
                    hashValue

                }

            } catch (err : Exception) {

                val message = "Opsy! Error generating hash for \"${document.canonicalPath}\""

                LOG.error(message, err)

                // At least we tried!
                throw OpsyException(message, err)

            }

        }

    }
}