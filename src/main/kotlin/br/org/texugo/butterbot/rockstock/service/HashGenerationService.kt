package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.OpsyException
import br.org.texugo.butterbot.rockstock.data.Document
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.experimental.and

class HashGenerationService {

    companion object {

        val LOG = LoggerFactory.getLogger(HashGenerationService::class.java)

        val BUFFER_SIZE = 1024

        @Throws(OpsyException::class)
        fun generateHash (document : Document) : String {

            LOG.debug("Generating hash for document ${document.canonicalPath}")

            return try {

                Files.newInputStream(Paths.get(document.canonicalPath)).use {

                    val buffer = ByteArray(BUFFER_SIZE)

                    val messageDigest = MessageDigest.getInstance(RockStockService.DIGEST_TYPE)

                    var read: Int

                    do {

                        read = it.read(buffer)

                        when {
                            read >= 0 -> messageDigest.update(buffer, 0, read)
                        }

                    } while (read >= 0)

                    val textBuilder = StringBuilder()

                    val hash = messageDigest.digest()

                    for (hashBit in hash) {

                        textBuilder.append(
                                ((hashBit and 0xff.toByte()) + 0x100).toString(16).substring(1)
                        )

                    }

                    val hashValue = textBuilder.toString()

                    LOG.debug("Hash $hashValue generated for ${document.canonicalPath}")

                    // Returning the hash!
                    textBuilder.toString()

                }

            } catch (err : Exception) {

                val message = "Opsy! Error generating hash for \"${document.canonicalPath}\""

                LOG.error(message, err)

                throw OpsyException(message, err)

            }

        }

    }
}