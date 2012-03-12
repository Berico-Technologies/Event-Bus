(function() {
  var root;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  root.NullBus = (function() {

    function NullBus() {
      console.info("Instantiating NullBus");
    }

    NullBus.prototype.publish = function(event, options) {
      return console.info("Publishing " + event + " with options: " + options);
    };

    NullBus.prototype.subscribeToEvent = function(eventHandler, options) {
      return console.info("Subscribing to Events with options: " + options);
    };

    NullBus.prototype.subscribeToEnvelope = function(envelopeHandler, options) {
      return console.info("Subscribing to Envelopes with options: " + options);
    };

    NullBus.prototype.unsubscribe = function(handler, options) {
      return console.info("Unsubscribing from events/envelopes with options: " + options);
    };

    NullBus.prototype.respondTo = function(request, response, options) {
      return console.info("Responding to request: " + request + " with response: " + response + " with options: " + options);
    };

    NullBus.prototype.getResponseTo = function(request, options) {
      return console.info("Getting response to request: " + request + " with options: " + options);
    };

    NullBus.prototype.handleResponseTo = function(request, handler, options) {
      return console.info("Handling response to request: " + request + " with options: " + options);
    };

    NullBus.prototype.start = function() {
      return console.info("Starting the bus");
    };

    NullBus.prototype.close = function() {
      return console.info("Closing the bus");
    };

    return NullBus;

  })();

}).call(this);
