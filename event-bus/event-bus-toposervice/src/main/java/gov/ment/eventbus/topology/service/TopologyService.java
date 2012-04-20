package gov.ment.eventbus.topology.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyService {

  private static final Logger LOG = LoggerFactory.getLogger(TopologyService.class);

  private RegistrationHandler registrationHandler;
  private UnknownEventTypeHandler unknownEventTypeHandler;

  public RegistrationHandler getRegistrationHandler() {
    return registrationHandler;
  }

  public void setRegistrationHandler(RegistrationHandler registrationHandler) {
    this.registrationHandler = registrationHandler;
  }

  public UnknownEventTypeHandler getUnknownEventTypeHandler() {
    return unknownEventTypeHandler;
  }

  public void setUnknownEventTypeHandler(UnknownEventTypeHandler unknownEventTypeHandler) {
    this.unknownEventTypeHandler = unknownEventTypeHandler;
  }

  public void start() {
    LOG.trace("Starting topology service.");
    registrationHandler.start();
    unknownEventTypeHandler.start();
  }

  public void stop() {
    LOG.trace("Stopping topology service.");
    unknownEventTypeHandler.stop();
    registrationHandler.stop();
  }
}
