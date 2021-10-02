package br.org.texugo.butterbot.rockstock.controller

import br.org.texugo.butterbot.rockstock.data.Document
import br.org.texugo.butterbot.rockstock.service.RockStockService
import org.apache.coyote.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * The entrance of the RockStock operations over document resources.
 *
 * It allows requests to stock files and also to retrieve a stocked file's location
 * based on its id (the stocked file name without the zip extension)
 */
@RestController
class RockStockController {

    // Our "Sancho Panza" object friend
    companion object {
        /** The RockStockController's logger */
        val LOG : Logger = LoggerFactory.getLogger(RockStockController::class.java)
    }

    // @TODO Use application.properties instead of hardcoding stuff here
    // @TODO https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
    /** The service we'll delegate the stock and retrieval orchestrations */
    private val rockStockService = RockStockService(
            1024,
            "/home/hollywoodpanda/Development/kotlin/rockstock_volume",
            "/home/hollywoodpanda/Development/kotlin/rockstock_volume_tmp"
    )

    /**
     * Retrieves a document from the RockStock storage system, in a GET http operation
     *
     * @param id The file identification (the file name without extension)
     *
     * @return ResponseEntity<Document> The found document with a 200 http status (if the operation went successfully)
     */
    @GetMapping("/document")
    fun rockIt (@RequestParam id : String) : ResponseEntity<Document> {

        // May the gods praise us with a success call!
        return try {

            // @TODO: Validate the id

            LOG.debug("Rocking $id")

            // Encapsulating the response on an OK http status
            ResponseEntity.ok(rockStockService.rockIt(id))

        } catch (err : Exception) {

            LOG.error("Error while rocking $id", err)

            // TODO: Better handle each type of possible error outcomes

            // Something went wrong, and we're
            // deciding to respond a not found
            // regardless of what really happened
            ResponseEntity.notFound().build()

        }

    }

    /**
     * Stocks the informed file in the RockStock storage system using a POST http operation
     *
     * @param document The holder of the file's canonical path
     *
     * @return ResponseEntity<Document> The created document with a 200 http status (if the operation went successfully)
     */
    @PostMapping("/document")
    fun stockIt (@RequestBody document : Document) : ResponseEntity<Document> {

        return try {

            // @TODO: Validate the given document and its canonical path

            LOG.debug("Stocking ${document.canonicalPath}")

            // Returning the stock operation result, if successful
            ResponseEntity.ok(rockStockService.stockIt(document))

        } catch (err : Exception) {

            // TODO: Better handle each type of possible error outcomes

            LOG.error("Error stocking ${document.canonicalPath}", err)

            // Something went wrong, and we're
            // deciding to respond an unprocessable entity
            // regardless of what really happened
            ResponseEntity.unprocessableEntity().build()

        }

    }

}