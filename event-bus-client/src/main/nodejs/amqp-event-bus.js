(function() {
  var AmqpEventBus;

  AmqpEventBus = (function() {

    function AmqpEventBus() {}

    AmqpEventBus.prototype.publish = function(event, options) {};

    AmqpEventBus.prototype.subscribeToEvent = function(eventHandler, options) {};

    AmqpEventBus.prototype.subscribeToEnvelope = function(envelopeHandler, options) {};

    AmqpEventBus.prototype.unsubscribe = function(handler, options) {};

    AmqpEventBus.prototype.respondTo = function(request, response, options) {};

    AmqpEventBus.prototype.getResponseTo = function(request, options) {};

    AmqpEventBus.prototype.handleResponseTo = function(request, handler, options) {};

    AmqpEventBus.prototype.start = function() {};

    AmqpEventBus.prototype.close = function() {};

    return AmqpEventBus;

  })();

}).call(this);
