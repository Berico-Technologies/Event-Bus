package gov.ment.eventbus.topology.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import gov.ment.eventbus.topology.service.RegistrationHandler;
import gov.ment.eventbus.topology.service.TopologyService;
import gov.ment.eventbus.topology.service.UnknownEventTypeHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TopologyServiceTest {

  private TopologyService topologyService;

  @Mock
  private RegistrationHandler registrationHandler;

  @Mock
  private UnknownEventTypeHandler unknownEventTypeHandler;

  @Before
  public void beforeEachTest() {

    MockitoAnnotations.initMocks(this);

    topologyService = new TopologyService();
    topologyService.setRegistrationHandler(registrationHandler);
    topologyService.setUnknownEventTypeHandler(unknownEventTypeHandler);
  }

  @Test
  public void testStart() {
    assertNotNull(topologyService);
    topologyService.start();
    verify(registrationHandler).start();
  }

  @Test
  public void stop() {
    assertNotNull(topologyService);
    topologyService.stop();
    verify(registrationHandler).stop();
  }
}
