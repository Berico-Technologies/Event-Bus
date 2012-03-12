
root = exports ? this

class root.NullBus

  constructor: ->
    console.info "Instantiating NullBus"

  publish: (event, options) ->
    console.info "Publishing " + event + " with options: " + options

  subscribeToEvent: (eventHandler, options) ->
    console.info "Subscribing to Events with options: " + options

  subscribeToEnvelope: (envelopeHandler, options) ->
    console.info "Subscribing to Envelopes with options: " + options

  unsubscribe: (handler, options) ->
    console.info "Unsubscribing from events/envelopes with options: " + options

  respondTo: (request, response, options) ->
    console.info "Responding to request: " + request + " with response: " + response + " with options: " + options

  getResponseTo: (request, options) ->
    console.info "Getting response to request: " + request + " with options: " + options

  handleResponseTo: (request, handler, options) ->
    console.info "Handling response to request: " + request + " with options: " + options

  start: () ->
    console.info "Starting the bus"

  close: () ->
    console.info "Closing the bus"