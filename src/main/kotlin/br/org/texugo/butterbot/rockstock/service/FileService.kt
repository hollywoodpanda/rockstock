package br.org.texugo.butterbot.rockstock.service

import br.org.texugo.butterbot.rockstock.data.Document
import java.io.File

/**
 * Extracts file names and directory names from documents.
 */
class FileService {

    companion object {

        /**
         * Returns the suffix of the document's canonical path, the file name.
         * Actually returns anything after the last [File.separator] in the string
         */
        fun extractDocumentFilename (document : Document) : String
                = document.canonicalPath.substring(document.canonicalPath.lastIndexOf(File.separator) + 1)

        /**
         * Returns the prefix of the document's canonical path, the directory path structure without the file.
         * Actually returns anything before the last [File.separator] in the string
         */
        fun extractDocumentDirectory (document : Document) : String
                = document.canonicalPath.substring(0, document.canonicalPath.lastIndexOf(File.separator))

    }

}