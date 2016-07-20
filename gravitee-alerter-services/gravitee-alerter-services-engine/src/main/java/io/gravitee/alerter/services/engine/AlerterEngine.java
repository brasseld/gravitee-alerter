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
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.event.rule.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
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

        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();

        KieBaseConfiguration config = ks.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);


        kieSession = kieContainer.newKieSession("alerter-session");

        kieSession.addEventListener(new RuleRuntimeEventListener() {
            public void objectInserted(ObjectInsertedEvent event) {
                System.out.println("Object inserted \n"
                        + event.getObject().toString());
            }

            public void objectUpdated(ObjectUpdatedEvent event) {
                System.out.println("Object was updated \n"
                        + "new Content \n" + event.getObject().toString());
            }

            public void objectDeleted(ObjectDeletedEvent event) {
                System.out.println("Object retracted \n"
                        + event.getOldObject().toString());
            }
        });

        kieSession.addEventListener(new AgendaEventListener() {
            public void matchCreated(MatchCreatedEvent event) {
                System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " can be fired in agenda");
            }

            public void matchCancelled(MatchCancelledEvent event) {
                System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " cannot b in agenda");
            }

            public void beforeMatchFired(BeforeMatchFiredEvent event) {
                System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " will be fired");
            }

            public void afterMatchFired(AfterMatchFiredEvent event) {
                System.out.println("The rule "
                        + event.getMatch().getRule().getName()
                        + " has be fired");
            }

            public void agendaGroupPopped(AgendaGroupPoppedEvent event) {

            }

            public void agendaGroupPushed(AgendaGroupPushedEvent event) {

            }

            public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {

            }

            public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {

            }

            public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {

            }

            public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {

            }
        });
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
                FactHandle toto = eventStream.insert(event.content());
                System.out.println("Facthandle="+toto.toExternalForm());
            } else {
                LOGGER.warn("An event has been received from an unknown stream: {}", streamName);
            }

        }
    }
}
