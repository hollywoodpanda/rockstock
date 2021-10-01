package br.org.texugo.butterbot.rockstock.controller

import br.org.texugo.butterbot.rockstock.data.Document
import br.org.texugo.butterbot.rockstock.service.RockStockService
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RockStockController {

    // @TODO Use application.properties instead of hardcoding stuff here
    // @TODO https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
    private val rockStockService = RockStockService(
            1024,
            "/home/hollywoodpanda/Development/kotlin/rockstock_volume",
            "/home/hollywoodpanda/Development/kotlin/rockstock_volume_tmp"
    )

    /**
     * Retrieves a document from the T4 storage
     */
    @GetMapping("/document")
    fun rockIt (@RequestParam id : String) : ResponseEntity<Document> {

        return try {

            ResponseEntity.ok(rockStockService.rockIt(id))

        } catch (err : Exception) {

            println("[DEBUG] Error in controller is ${err.message}")

            ResponseEntity.notFound().build()

        }

    }

    @PostMapping("/document")
    fun stockIt (@RequestBody document : Document) : ResponseEntity<Document> {

        return try {

            // Returning the stock operation result, if successful
            ResponseEntity.ok(rockStockService.stockIt(document))

        } catch (err : Exception) {

            println("[DEBUG] Some error stocking it: ${err.message}")

            ResponseEntity.unprocessableEntity().build()

        }

    }

}