class AmqpEventBus

  constructor: ->

  publish: (event, options) ->

  subscribeToEvent: (eventHandler, options) ->

  subscribeToEnvelope: (envelopeHandler, options) ->

  unsubscribe: (handler, options) ->

  respondTo: (request, response, options) ->

  getResponseTo: (request, options) ->

  handleResponseTo: (request, handler, options) ->

  start: ->

  close: ->
