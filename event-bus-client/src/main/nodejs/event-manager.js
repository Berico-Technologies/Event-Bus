(function() {
  var root;

  root = typeof exports !== "undefined" && exports !== null ? exports : this;

  root.EventManager = (function() {
    /*
         Instantiate the Event Manager
    */
    function EventManager(config) {
      var _ref, _ref2, _ref3, _ref4;
      this.config = config;
      console.info("Instantiating the Event Manager");
      this.bus = config.bus;
      this.startListeners = (_ref = this.config.onStart) != null ? _ref : [];
      this.closeListeners = (_ref2 = this.config.onClose) != null ? _ref2 : [];
      this.subListeners = (_ref3 = this.config.onSubscribe) != null ? _ref3 : [];
      this.unsubListeners = (_ref4 = this.config.onUnsubscribe) != null ? _ref4 : [];
    }

    /*
         Start the Event Bus
    */

    EventManager.prototype.start = function() {
      var notify, _i, _len, _ref, _results;
      this.bus.start();
      _ref = this.startListeners;
      _results = [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        notify = _ref[_i];
        _results.push(notify());
      }
      return _results;
    };

    /*
         Shutdown the Event Bus
    */

    EventManager.prototype.close = function() {
      var notify, _i, _len, _ref;
      _ref = this.closeListeners;
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        notify = _ref[_i];
        notify();
      }
      return this.bus.stop;
    };

    EventManager.prototype.publish = function(event) {
      return this.bus.publish(event);
    };

    EventManager.prototype.onEvent = function(eventType, eventHandler, queueName) {};

    EventManager.prototype.onEnvelope = function(eventSet, envelopeHandler, queueName) {};

    EventManager.prototype.getResponseTo = function(event, eventHandler) {};

    EventManager.prototype.waitForResponse = function(event, timeout) {};

    EventManager.prototype.respondTo = function(event, response) {};

    EventManager.prototype.unbind = function(handler) {};

    EventManager.prototype.onStart = function(startHandler) {
      return this.startListeners.push(startHandler);
    };

    EventManager.prototype.onClose = function(closeHandler) {
      return this.closeListeners.push(closeHandler);
    };

    EventManager.prototype.onSubscribe = function(onSubHandler) {
      return this.subListeners.push(onSubHandler);
    };

    EventManager.prototype.onUnsubscribe = function(onUnsubHandler) {
      return this.unsubListeners.push(onUnsubHandler);
    };

    EventManager.prototype.attach = function(lifecycleEvent, handler) {
      switch (lifecycleEvent) {
        case "start":
          return onStart(handler);
        case "close":
          return onClose(handler);
        case "subscribe":
          return onSubscribe(handler);
        case "unsubscribe":
          return onUnsubscribe(handler);
      }
    };

    EventManager.prototype.detach = function(handler) {};

    /*
      Method Aliases
    */

    return EventManager;

  })();

}).call(this);
