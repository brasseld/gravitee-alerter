package io.gravitee.alerter.services.engine.drools;

import org.kie.api.event.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AgendaEventListenerLogger extends DefaultAgendaEventListener {

    private final Logger LOGGER = LoggerFactory.getLogger(AgendaEventListenerLogger.class);

    public void matchCreated(MatchCreatedEvent event) {
        LOGGER.debug("The rule {} can be fired in agenda", event.getMatch().getRule().getName());
    }

    public void matchCancelled(MatchCancelledEvent event) {
        LOGGER.debug("The rule {} cannot be in agenda", event.getMatch().getRule().getName());
    }

    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        LOGGER.debug("The rule {} will be fired", event.getMatch().getRule().getName());
    }

    public void afterMatchFired(AfterMatchFiredEvent event) {
        LOGGER.debug("The rule {} has been fired", event.getMatch().getRule().getName());
    }
}
