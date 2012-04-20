package gov.ment.eventbus.topology.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import gov.ment.eventbus.topology.service.UnknownEventTypeHandler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import gov.ment.eventbus.amqp.RoutingInfo;
import gov.ment.eventbus.amqp.RoutingInfo.ExchangeType;
import gov.ment.eventbus.client.EventManager;
import gov.ment.eventbus.client.SubscriptionToken;
import gov.ment.eventbus.topology.TopologyRegistry;
import gov.ment.eventbus.topology.events.EventTypeRoutingInfo;
import gov.ment.eventbus.topology.events.GetEventTypeRoute;
import gov.ment.eventbus.topology.events.TopologyUpdate;

public class UnknownEventTypeHandlerTest {

  private UnknownEventTypeHandler handler;
  @Mock
  private EventManager eventManager;
  @Mock
  private TopologyRegistry topologyRegistry;
  @Mock
  private SubscriptionToken subscriptionToken;

  private String newEventTypeName = UUID.randomUUID().toString();
  GetEventTypeRoute incommingRequest = new GetEventTypeRoute(newEventTypeName);
  private RoutingInfo expectedRoute = new RoutingInfo("test", ExchangeType.Topic, true,
          newEventTypeName);

  @Before
  public void beforeEachTest() {
    MockitoAnnotations.initMocks(this);

    handler = new UnknownEventTypeHandler();
    handler.setEventManager(eventManager);
    handler.setTopologyRegistry(topologyRegistry);
  }

  @Test
  public void startingTheHandlerShouldSubscribeTheHandler() {
    when(eventManager.subscribe(handler)).thenReturn(subscriptionToken);
    handler.start();
    verify(eventManager).subscribe(handler);
  }

  @Test
  public void stoppingTheHandlerShouldUnsubscribeTheHandler() {
    when(eventManager.subscribe(handler)).thenReturn(subscriptionToken);
    handler.start();
    handler.stop();
    verify(eventManager).unsubscribe(subscriptionToken);
  }

  @Test
  @Ignore
  public void handlingGetEventTypeRouteShouldRespondWithExpectedRoute() {
    handler.handleEvent(incommingRequest);
    ArgumentCaptor<EventTypeRoutingInfo> responseCaptor =
            ArgumentCaptor.forClass(EventTypeRoutingInfo.class);
    verify(eventManager).respondTo(eq(incommingRequest), responseCaptor.capture());
    assertEquals(newEventTypeName, responseCaptor.getValue().getEventTypeCannonicalName());
    assertEquals(expectedRoute, responseCaptor.getValue().getRouteInfo());
  }

  @Test
  @Ignore
  public void handlingGetEventTypeRouteShouldUpdateTheTopologyAndPublishTheUpdatedVersion() {
    InOrder inOrder = inOrder(topologyRegistry, eventManager);
    handler.handleEvent(incommingRequest);
    ArgumentCaptor<TopologyUpdate> updateCaptor = ArgumentCaptor.forClass(TopologyUpdate.class);
    inOrder.verify(topologyRegistry).setEventRoute(newEventTypeName, expectedRoute);
    inOrder.verify(eventManager).publish(updateCaptor.capture());
    assertEquals(topologyRegistry, updateCaptor.getValue().getTopologyRegistry());
  }
}
