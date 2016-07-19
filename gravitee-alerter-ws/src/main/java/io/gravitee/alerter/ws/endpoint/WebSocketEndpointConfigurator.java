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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class WebSocketEndpointConfigurator extends ServerEndpointConfig.Configurator
        implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEndpointConfigurator.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void modifyHandshake(final ServerEndpointConfig conf,
                                final HandshakeRequest req,
                                final HandshakeResponse resp) {
        conf.getUserProperties().put("handshakeRequest", req);
    }

    @Override
    public boolean checkOrigin(final String originHeaderValue) {
        LOGGER.info("Origin: {}", originHeaderValue);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(final Class<T> endpointClass) throws InstantiationException {
        return (T) applicationContext.getBean("webSocketEndPoint", WebSocketEndpoint.class);
    }
}
