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

import io.gravitee.alerter.model.EventType;
import io.gravitee.alerter.model.healthcheck.HealtcheckEvent;
import io.gravitee.common.event.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlerterEngineTest {

    private final AlerterEngine engine = new AlerterEngine();

    @Before
    public void setUp() throws Exception {
        engine.doStart();
    }

    @After
    public void tearDown() throws Exception {
        engine.doStop();
    }

    @Test
    public void test_healthcheck() {
        engine.onEvent(new Event<EventType, io.gravitee.alerter.model.Event>() {
            @Override
            public io.gravitee.alerter.model.Event content() {
                HealtcheckEvent evt = new HealtcheckEvent();

                evt.setDate(new Date());
                evt.setApi("my-api");
                evt.setResponseTime(10000);
                evt.setStatus(HealtcheckEvent.Status.DOWN);
                return evt;
            }

            @Override
            public EventType type() {
                return EventType.FIRE_EVENT;
            }
        });
    }
}
