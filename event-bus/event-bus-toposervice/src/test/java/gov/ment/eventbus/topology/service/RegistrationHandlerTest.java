package gov.ment.eventbus.topology.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import gov.ment.eventbus.topology.service.ClientRegistry;
import gov.ment.eventbus.topology.service.RegistrationHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.ment.eventbus.client.EventManager;
import gov.ment.eventbus.client.EventResult;
import gov.ment.eventbus.client.SubscriptionToken;
import gov.ment.eventbus.topology.TopologyRegistry;
import gov.ment.eventbus.topology.events.RegisterClient;
import gov.ment.eventbus.topology.events.Registration;
import gov.ment.eventbus.topology.events.UnregisterClient;

public class RegistrationHandlerTest {

  private RegistrationHandler registrationHandler;
  @Mock
  private EventManager eventManager;
  @Mock
  private ClientRegistry clientRegistry;
  @Mock
  private TopologyRegistry topologyRegistry;
  @Mock
  private SubscriptionToken subscriptionToken;

  @Before
  public void beforeEachTest() {
    MockitoAnnotations.initMocks(this);

    registrationHandler = new RegistrationHandler();
    registrationHandler.setEventManager(eventManager);
    registrationHandler.setClientRegistry(clientRegistry);
    registrationHandler.setTopologyRegistry(topologyRegistry);
  }

  @Test
  public void startingTheHandlerShouldSubscribeTheHandler() {
    when(eventManager.subscribe(registrationHandler)).thenReturn(subscriptionToken);
    registrationHandler.start();
    verify(eventManager).subscribe(registrationHandler);
  }

  @Test
  public void stoppingTheHandlerShouldUnsubscribeTheHandler() {
    when(eventManager.subscribe(registrationHandler)).thenReturn(subscriptionToken);
    registrationHandler.start();
    registrationHandler.stop();
    verify(eventManager).unsubscribe(subscriptionToken);
  }

  @Test
  public void getHandledEventTypesTest() {
    List<Class<Registration>> handledEventTypes =
            Arrays.asList(registrationHandler.getHandledEventTypes());
    assertTrue(handledEventTypes.contains(RegisterClient.class));
    assertTrue(handledEventTypes.contains(UnregisterClient.class));
  }

  @Test
  public void handleRegisterClientEventTest() {
    String clientName = "clientName";
    String version = "1.0";
    RegisterClient registerEvent = new RegisterClient(clientName, version);
    assertEquals(EventResult.Handled, registrationHandler.handleEvent(registerEvent));
    verify(clientRegistry).registerClient(registerEvent);
  }

  @Test
  public void handleUnregisterClientEventTest() {
    String clientName = "clientName";
    UnregisterClient unregisterEvent = new UnregisterClient(clientName);
    assertEquals(EventResult.Handled, registrationHandler.handleEvent(unregisterEvent));
    verify(clientRegistry).unregisterClient(unregisterEvent);
  }

  @Test
  public void handleNullEventTest() {
    assertEquals(EventResult.Failed, registrationHandler.handleEvent(null));
  }

  @Test
  public void exceptionInHandleEventTest() {
    assertEquals(EventResult.Failed, registrationHandler.handleEvent(null));
  }

}
