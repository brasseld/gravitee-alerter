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
package io.gravitee.alerter.services.engine.healthcheck;

import io.gravitee.alerter.model.healthcheck.HealthcheckEvent;
import io.gravitee.alerter.services.engine.AlerterEngineTest;
import io.gravitee.alerter.services.engine.contract.healthcheck.ConsecutiveFailuresRule;
import io.gravitee.alerter.services.engine.contract.healthcheck.ResponseTimeExceededRule;
import io.gravitee.alerter.services.engine.contract.healthcheck.alert.Alert;
import io.gravitee.alerter.services.engine.contract.healthcheck.alert.ConsecutiveFailureAlert;
import io.gravitee.alerter.services.engine.contract.healthcheck.alert.ResponseTimeExceededAlert;
import io.gravitee.alerter.services.engine.contract.healthcheck.alert.StateChangedAlert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class HealthcheckAlertTest extends AlerterEngineTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ConsecutiveFailuresRule rule = new ConsecutiveFailuresRule("my-api");
        rule.setEndpoint("http://localhost:9099/my-api/status");
        rule.setFailure(5);
        addRule(rule);

        ConsecutiveFailuresRule rule2 = new ConsecutiveFailuresRule("my-api2");
        rule2.setEndpoint("http://localhost:9099/my-api/status");
        rule2.setFailure(5);
        addRule(rule2);

        ResponseTimeExceededRule rule3 = new ResponseTimeExceededRule("my-api");
        rule3.setEndpoint("http://localhost:9099/my-api/status");
        rule3.setMax(500);
        rule3.setCounter(5);
        addRule(rule3);
    }

    @Test
    public void test_healthcheck_noAlert() {
        for (int i = 0 ; i < 3  ; i++) {
            HealthcheckEvent evt = create();
            evt.setStatus(HealthcheckEvent.Status.DOWN);

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.never()).send(any(Alert.class));
    }

    @Test
    public void test_healthcheck_alert_consecutiveFailures() {
        Instant instant = Instant.now();

        for(int i = 0 ; i < 5 ; i++) {
            HealthcheckEvent evt = create();
            evt.setDate(Date.from(instant.plus(i + 5, ChronoUnit.SECONDS)));
            evt.setStatus(HealthcheckEvent.Status.DOWN);

            sendEvent(evt);
        }

        // Move to next minute
        instant = instant.plus(2, ChronoUnit.MINUTES);

        for(int i = 0 ; i < 5 ; i++) {
            HealthcheckEvent evt = create();
            evt.setDate(Date.from(instant.plus(i + 5, ChronoUnit.SECONDS)));
            evt.setStatus(HealthcheckEvent.Status.DOWN);

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.times(1)).send(isA(ConsecutiveFailureAlert.class));
    }

    @Test
    public void test_healthcheck_alert_multipleConsecutiveFailures() {
        for(int i = 0 ; i < 10 ; i++) {
            HealthcheckEvent evt = create();

            if (i % 2 == 0) {
                evt.setStatus(HealthcheckEvent.Status.UP);
            } else {
                evt.setStatus(HealthcheckEvent.Status.DOWN);
            }
            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.never()).send(isA(ConsecutiveFailureAlert.class));
    }

    @Test
    public void test_healthcheck_alert_noConsecutiveFailures() {
        for(int i = 0 ; i < 10 ; i++) {
            HealthcheckEvent evt = create();

            if (i % 2 == 0) {
                evt.setStatus(HealthcheckEvent.Status.UP);
            } else {
                evt.setStatus(HealthcheckEvent.Status.DOWN);
            }

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.never()).send(isA(ConsecutiveFailureAlert.class));
    }

    @Test
    public void test_healthcheck_alert_stateChanged() {
        for(int i = 0 ; i < 3 ; i++) {
            HealthcheckEvent evt = create();

            if (i % 2 == 0) {
                evt.setStatus(HealthcheckEvent.Status.UP);
            } else {
                evt.setStatus(HealthcheckEvent.Status.DOWN);
            }

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.times(2)).send(isA(StateChangedAlert.class));
    }

    @Test
    public void test_healthcheck_alert_responseTimeExceeded() {
        for(int i = 0 ; i < 10 ; i++) {
            HealthcheckEvent evt = create();
            evt.setStatus(HealthcheckEvent.Status.UP);

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.times(1)).send(isA(ResponseTimeExceededAlert.class));
    }

    @Test
    public void test_healthcheck_alert_noResponseTimeExceeded() {
        for(int i = 0 ; i < 10 ; i++) {
            HealthcheckEvent evt = create();
            evt.setStatus(HealthcheckEvent.Status.UP);

            if (i % 2 == 0) {
                evt.setResponseTime(200);
            }

            sendEvent(evt);
        }

        fireAndWait();
        verify(alertService, Mockito.never()).send(isA(ResponseTimeExceededAlert.class));
    }

    private HealthcheckEvent create() {
        HealthcheckEvent evt = new HealthcheckEvent();

        evt.setDate(new Date());
        evt.setApi("my-api");
        evt.setEndpoint("http://localhost:9099/my-api/status");
        evt.setResponseTime(10000);

        return evt;
    }
}
