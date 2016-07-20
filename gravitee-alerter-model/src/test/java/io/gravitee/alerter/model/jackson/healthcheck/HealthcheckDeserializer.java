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
package io.gravitee.alerter.model.jackson.healthcheck;

import io.gravitee.alerter.model.healthcheck.HealthcheckEvent;
import io.gravitee.alerter.model.jackson.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class HealthcheckDeserializer extends AbstractTest {

    @Test
    public void should_deserialize_healthcheck() throws Exception {
        HealthcheckEvent healthcheckEvent = load("/io/gravitee/alerter/model/jackson/healthcheck.json", HealthcheckEvent.class);

        Assert.assertNotNull(healthcheckEvent);
        Assert.assertEquals(HealthcheckEvent.Status.DOWN, healthcheckEvent.getStatus());
    }
}
