package br.org.texugo.butterbot.rockstock.controller

import br.org.texugo.butterbot.rockstock.data.Document
import br.org.texugo.butterbot.rockstock.service.RockStockService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

            ResponseEntity.notFound().build()

        }

    }

}