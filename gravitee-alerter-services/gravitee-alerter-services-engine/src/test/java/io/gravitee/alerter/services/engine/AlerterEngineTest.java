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
import io.gravitee.alerter.services.engine.contract.healthcheck.alert.Alert;
import org.junit.After;
import org.junit.Before;
import org.mockito.Spy;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlerterEngineTest {

    private final AlerterEngine engine = new AlerterEngine();

    @Spy
    protected AlertService alertService = new ConsoleAlertService();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        engine.setAlertService(alertService);
        engine.doStart();
    }

    @After
    public void tearDown() throws Exception {
        engine.doStop();
    }

    protected void sendEvent(final Event event) {
        engine.onEvent(new io.gravitee.common.event.Event<EventType, Event>() {
            @Override
            public Event content() {
                return event;
            }

            @Override
            public EventType type() {
                return EventType.FIRE_EVENT;
            }
        });
    }

    protected void addRule(final Rule rule) {
        engine.insertRule(rule);
    }

    protected void fireAndWait() {
        try {
            // give time to fireUntilHalt to process the insertions
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ConsoleAlertService implements AlertService {

        @Override
        public void send(Alert alert) {
            System.out.println(alert);
        }
    }
}
