
class EventManager

  ###
     Instantiate the Event Manager
  ###
  constructor: (@config) ->
    @amqp = @config.amqp ? require 'amqp'
    @startListeners = @config.onStart ? []
    @closeListeners = @config.onClose ? []
    @subListeners = @config.onSubscribe ? []
    @unsubListeners = @config.onUnsubscribe ? []

  ###
     Start the Event Bus
  ###
  start: () ->
    # Open the Connection to the Event Bus
    @conn = @amqp.createConnection(@config)
    # Loop through the start listeners, notifying
    # each one that the Event Manager has started
    notify() for notify in @startListeners

  ###
     Shutdown the Event Bus
  ###
  close: () ->
    # Loop through the close listeners notifying
    # them that the bus is shutting down
    notify() for notify in @closeListeners
    # Close the connection
    @conn.end

  publish: (event) ->


  onEvent: (eventType, eventHandler, queueName) ->


  onEnvelope: (eventSet, envelopeHandler, queueName) ->


  getResponseTo: (event, eventHandler) ->


  waitForResponse: (event, timeout) ->


  respondTo: (event, response) ->


  unbind: (handler) ->


  onStart: (startHandler) ->


  onClose: (closeHandler) ->


  onSubscribe: (onSubHandler) ->


  onUnsubscribe: (onUnsubHandler) ->


  attach: (lifecycleEvent, handler) ->


  detach: (handler) ->




  ###
  Method Aliases
  ###

  ###
    Same as 'onEvent'
    @param eventType Type of event to subscribe to.
    @param eventHandler Function that will handle the event.
    @param queueName [optional] specify the named queue to bind
                                the handler to.
  ###
  subscribeToEvent: (eventType, eventHandler, queueName) ->
    onEvent(eventType, eventHandler, queueName)

  ###
    Same as 'onEnvelope'
    @param eventSet Named set of events to subscribe to.
    @param envelopeHandler Function that will handle the message
                           envelope when it is received.
    @param queueName [optional] specify the named queue to bind
                                the handler to.
  ###
  subscribeToEnvelope: (eventSet, envelopeHandler, queueName) ->
    onEnvelope(eventSet, envelopeHandler, queueName)