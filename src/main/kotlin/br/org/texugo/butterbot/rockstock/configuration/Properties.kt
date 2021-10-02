package br.org.texugo.butterbot.rockstock.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * The properties holder, filled by the Spring gods
 */
@Configuration
@PropertySource("classpath:application.properties")
class Properties {

    /**
     * The configured volume in application.properties
     * We must be vocal about the final stuff, so we
     * can privatize the setter (Spring opens everything)
     */
    @Value("\${butterbot.rockstock.volume}")
    final lateinit var volume : String
        private set

    /**
     * The configured temporary volume in application.properties
     * We must be vocal about the final stuff, so we
     * can privatize the setter (Spring opens everything)
     */
    @Value("\${butterbot.rockstock.tempvolume}")
    final lateinit var temporaryVolume : String
        private set

    /** Our 'Sancho Panza' object friend */
    companion object {

        /** The initialized by Spring gods properties instance */
        lateinit var instance : Properties

    }

}