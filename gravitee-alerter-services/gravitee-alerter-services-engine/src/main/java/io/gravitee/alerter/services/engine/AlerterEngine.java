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
import io.gravitee.alerter.services.engine.contract.Rule;
import io.gravitee.alerter.services.engine.drools.AgendaEventListenerLogger;
import io.gravitee.alerter.services.engine.drools.RuleRuntimeEventListenerLogger;
import io.gravitee.common.event.EventListener;
import io.gravitee.common.service.AbstractService;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlerterEngine extends AbstractService implements EventListener<EventType, Event> {

    private final Logger LOGGER = LoggerFactory.getLogger(AlerterEngine.class);
    private final static String SERVICE_NAME = "Alerter Engine - Drools";

    private KieSession kieSession;

    private AlertService alertService;

    private ExecutorService droolsThread;
    private Future fireUntilHaltResult;

    @Override
    protected String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();

        KieBaseConfiguration config = ks.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        kieSession = kieContainer.newKieSession("alerter-session");

        kieSession.addEventListener(new RuleRuntimeEventListenerLogger());
        kieSession.addEventListener(new AgendaEventListenerLogger());

        Globals globals = kieSession.getGlobals();
        globals.set("alerterService", alertService);

        droolsThread = Executors.newSingleThreadExecutor();
        fireUntilHaltResult = droolsThread.submit(() -> kieSession.fireUntilHalt());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        if (kieSession != null) {
            kieSession.halt();

            // wait for the engine to finish and throw exception if any was thrown in engine's thread
            fireUntilHaltResult.get(60000, TimeUnit.SECONDS);
            droolsThread.shutdown();

            kieSession.destroy();
        }
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

    public void insertRule(Rule rule) {
        kieSession.insert(rule);
    }

    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }
}
