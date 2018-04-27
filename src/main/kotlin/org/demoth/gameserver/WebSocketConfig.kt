package org.demoth.gameserver

import org.demoth.gameserver.MessageHandler
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@EnableWebSocket
@Configuration
open class WebSocketConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(createHandler(), "/action.json")
                .addInterceptors(HttpSessionHandshakeInterceptor())
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    open fun createHandler(): WebSocketHandler {
        return MessageHandler()
    }
}
