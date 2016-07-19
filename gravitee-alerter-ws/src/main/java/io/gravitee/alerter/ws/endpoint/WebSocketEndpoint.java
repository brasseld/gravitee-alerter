/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.alerter.ws.endpoint;

import io.gravitee.alerter.model.Event;
import io.gravitee.alerter.model.EventType;
import io.gravitee.alerter.ws.encoder.EventDecoder;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
@ServerEndpoint(
        value = "/",
        decoders = { EventDecoder.class }
)
public class WebSocketEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEndpoint.class);

    @Autowired
    private EventManager eventManager;

    /**
     * Invoked on connect.
     *
     * @param session The session object for this connection.
     * @param endpointConfig THe endpoint configuration.
     */
    @OnOpen
    public void onWebSocketConnect(final Session session, final EndpointConfig endpointConfig) {
        // Infinite idle timeout
        session.setMaxIdleTimeout(0L);

        HandshakeRequest request = (HandshakeRequest) endpointConfig.getUserProperties().get("handshakeRequest");

        LOGGER.info("Socket Opened: {}", session.getId());
        LOGGER.info("User Agent: {}", request.getHeaders().get(HttpHeaders.USER_AGENT));
    }

    @OnMessage
    public void onMessage(final Session session, final Event event) throws IOException {
        LOGGER.debug("Event received from {}: {}", session.getId(), event);
//        session.getBasicRemote().sendText(event.toString());
        eventManager.publishEvent(EventType.FIRE_EVENT, event);
    }

    /**
     * Invoked the connection is closed.
     *
     * @param session The session.
     * @param reason The reason the connection was closed.
     */
    @OnClose
    public void onWebSocketClose(final Session session, final CloseReason reason) {
        LOGGER.info("Socket {} Closed: {}", session.getId(), reason.getReasonPhrase());
    }
}
