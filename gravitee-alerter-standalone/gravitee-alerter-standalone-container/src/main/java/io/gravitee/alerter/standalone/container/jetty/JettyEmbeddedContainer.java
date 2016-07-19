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
package io.gravitee.alerter.standalone.container.jetty;

import io.gravitee.alerter.ws.endpoint.WebSocketEndpoint;
import io.gravitee.alerter.ws.endpoint.WebSocketEndpointConfigurator;
import io.gravitee.common.component.AbstractLifecycleComponent;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.AnnotatedServerEndpointConfig;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.websocket.server.ServerEndpoint;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public final class JettyEmbeddedContainer extends AbstractLifecycleComponent<JettyEmbeddedContainer> implements ApplicationContextAware {

    @Autowired
    private Server server;

    private ApplicationContext applicationContext;

    @Override
    protected void doStart() throws Exception {
        // Create the servlet context
        final ServletContextHandler context = new ServletContextHandler(server, "/*", ServletContextHandler.SESSIONS);
        server.setHandler(context);

        // Initialize javax.websocket layer
        ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
        wscontainer.start();

        // Add WebSocket endpoint to javax.websocket layer
        wscontainer.addEndpoint(
                new AnnotatedServerEndpointConfig(
                        wscontainer,
                        WebSocketEndpoint.class,
                        WebSocketEndpoint.class.getAnnotation(ServerEndpoint.class)
                ) {
                    @Override
                    public Configurator getConfigurator() {
                        return applicationContext.getBean(WebSocketEndpointConfigurator.class);
                    }
                });

        // start the server
        server.start();
        server.join();
    }

    @Override
    protected void doStop() throws Exception {
        server.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}