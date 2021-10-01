package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.data.Document
import java.io.File

class FileService {

    companion object {

        fun extractDocumentFilename (document : Document) : String
                = document.canonicalPath.substring(document.canonicalPath.lastIndexOf(File.separator) + 1)

        fun extractDocumentDirectory (document : Document) : String
                = document.canonicalPath.substring(0, document.canonicalPath.lastIndexOf(File.separator))

    }

}