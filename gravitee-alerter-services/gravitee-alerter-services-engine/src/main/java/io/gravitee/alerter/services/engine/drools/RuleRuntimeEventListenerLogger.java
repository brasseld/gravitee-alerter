package io.gravitee.alerter.services.engine.drools;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RuleRuntimeEventListenerLogger implements RuleRuntimeEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(RuleRuntimeEventListenerLogger.class);

    public void objectInserted(ObjectInsertedEvent event) {
        LOGGER.debug("Object inserted: {}", event.getObject().toString());
    }

    public void objectUpdated(ObjectUpdatedEvent event) {
        LOGGER.debug("Object updated: {}", event.getObject().toString());
    }

    public void objectDeleted(ObjectDeletedEvent event) {
        LOGGER.debug("Object retracted: {}", event.getOldObject().toString());
    }
}
