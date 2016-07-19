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
package io.gravitee.alerter.services.engine;

import io.gravitee.alerter.model.Event;
import io.gravitee.alerter.model.EventType;
import io.gravitee.common.event.EventListener;
import io.gravitee.common.service.AbstractService;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlerterEngine extends AbstractService implements EventListener<EventType, Event> {

    private final Logger LOGGER = LoggerFactory.getLogger(AlerterEngine.class);
    private final static String SERVICE_NAME = "Alerter Engine - Drools";

    private KieSession kieSession;

    @Override
    protected String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kFileSystem = kieServices.newKieFileSystem();

        Resource resource = kieServices.getResources()
                .newClassPathResource("io/gravitee/alerter/services/engine/rules.drl", AlerterEngine.class)
                .setResourceType(ResourceType.DRL);

        kFileSystem.write(resource);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if( results.hasMessages( Message.Level.ERROR ) ){
            System.out.println( results.getMessages() );
            throw new IllegalStateException( "### errors ###" );
        }

        KieContainer kieContainer =
                kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

        KieBaseConfiguration config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        KieBase kieBase = kieContainer.newKieBase(config);
        kieSession = kieBase.newKieSession();

        new Thread() {

            @Override
            public void run() {
                kieSession.fireUntilHalt();
            }
        }.start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        kieSession.destroy();
    }

    @Override
    public void onEvent(io.gravitee.common.event.Event<EventType, Event> event) {
        if (event.type() == EventType.FIRE_EVENT) {
            // Get stream per event type
            String streamName = event.content().getClass().getSimpleName();

            EntryPoint eventStream = kieSession.getEntryPoint(streamName);
            if (eventStream != null) {
                eventStream.insert(event.content());
            } else {
                LOGGER.warn("An event has been received from an unknown stream: {}", streamName);
            }
        }
    }
}
