package br.org.texugo.butterbot.rockstock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RockStockApplication

fun main(args: Array<String>) {
	runApplication<RockStockApplication>(*args)
}
