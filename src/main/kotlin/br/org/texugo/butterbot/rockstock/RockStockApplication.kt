package br.org.texugo.butterbot.rockstock

import br.org.texugo.butterbot.rockstock.configuration.Properties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RockStockApplication

/**
 * The starting point of the whole operation
 * It starts the spring boot magical stuff and
 * our API starts listening to requests
 */
fun main(args: Array<String>) {
	// We need this context so we can start the Properties.instance below
	val context = runApplication<RockStockApplication>(*args)
	// Starting the Properties.instance for the whole API
	Properties.instance = context.getBean(Properties::class.java)
}
