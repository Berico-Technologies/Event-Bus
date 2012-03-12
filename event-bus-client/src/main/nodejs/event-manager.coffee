
root = exports ? this

class root.EventManager

  ###
     Instantiate the Event Manager
  ###
  constructor: (@config) ->
    console.info "Instantiating the Event Manager"
    @bus = config.bus
    @startListeners = @config.onStart ? []
    @closeListeners = @config.onClose ? []
    @subListeners = @config.onSubscribe ? []
    @unsubListeners = @config.onUnsubscribe ? []

  ###
     Start the Event Bus
  ###
  start: () ->
    # Start the underlying bus implementation
    @bus.start()
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
    @bus.stop

  publish: (event) ->
    @bus.publish(event)

  onEvent: (eventType, eventHandler, queueName) ->


  onEnvelope: (eventSet, envelopeHandler, queueName) ->


  getResponseTo: (event, eventHandler) ->


  waitForResponse: (event, timeout) ->


  respondTo: (event, response) ->


  unbind: (handler) ->


  onStart: (startHandler) ->
    @startListeners.push(startHandler)

  onClose: (closeHandler) ->
    @closeListeners.push(closeHandler)

  onSubscribe: (onSubHandler) ->
    @subListeners.push(onSubHandler)

  onUnsubscribe: (onUnsubHandler) ->
    @unsubListeners.push(onUnsubHandler)

  attach: (lifecycleEvent, handler) ->
    switch lifecycleEvent
      when "start" then onStart(handler)
      when "close" then onClose(handler)
      when "subscribe" then onSubscribe(handler)
      when "unsubscribe" then onUnsubscribe(handler)

  detach: (handler) ->




  ###
  Method Aliases
  ###

