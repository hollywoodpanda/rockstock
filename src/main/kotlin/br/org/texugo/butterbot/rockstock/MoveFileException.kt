package br.org.texugo.butterbot.rockstock

class MoveFileException (fromFileName : String, toFileName : String) : RuntimeException("Error moving file \"$fromFileName\" to \"$toFileName\"") {
}