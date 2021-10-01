package br.org.texugo.butterbot.rockstock

class DocumentNotFoundException(documentId : String) : RuntimeException("Document $documentId not found") {



}