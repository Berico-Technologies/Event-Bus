(function() {
  var EventManager;

  EventManager = (function() {
    /*
         Instantiate the Event Manager
    */
    function EventManager(config) {
      var _ref, _ref2, _ref3, _ref4, _ref5;
      this.config = config;
      this.amqp = (_ref = this.config.amqp) != null ? _ref : require('amqp');
      this.startListeners = (_ref2 = this.config.onStart) != null ? _ref2 : [];
      this.closeListeners = (_ref3 = this.config.onClose) != null ? _ref3 : [];
      this.subListeners = (_ref4 = this.config.onSubscribe) != null ? _ref4 : [];
      this.unsubListeners = (_ref5 = this.config.onUnsubscribe) != null ? _ref5 : [];
    }

    /*
         Start the Event Bus
    */

    EventManager.prototype.start = function() {
      var notify, _i, _len, _ref, _results;
      this.conn = this.amqp.createConnection(this.config);
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
      return this.conn.end;
    };

    EventManager.prototype.publish = function(event) {};

    EventManager.prototype.onEvent = function(eventType, eventHandler, queueName) {};

    EventManager.prototype.onEnvelope = function(eventSet, envelopeHandler, queueName) {};

    EventManager.prototype.getResponseTo = function(event, eventHandler) {};

    EventManager.prototype.waitForResponse = function(event, timeout) {};

    EventManager.prototype.respondTo = function(event, response) {};

    EventManager.prototype.unbind = function(handler) {};

    EventManager.prototype.onStart = function(startHandler) {};

    EventManager.prototype.onClose = function(closeHandler) {};

    EventManager.prototype.onSubscribe = function(onSubHandler) {};

    EventManager.prototype.onUnsubscribe = function(onUnsubHandler) {};

    EventManager.prototype.attach = function(lifecycleEvent, handler) {};

    EventManager.prototype.detach = function(handler) {};

    /*
      Method Aliases
    */

    /*
        Same as 'onEvent'
        @param eventType Type of event to subscribe to.
        @param eventHandler Function that will handle the event.
        @param queueName [optional] specify the named queue to bind
                                    the handler to.
    */

    EventManager.prototype.subscribeToEvent = function(eventType, eventHandler, queueName) {
      return onEvent(eventType, eventHandler, queueName);
    };

    /*
        Same as 'onEnvelope'
        @param eventSet Named set of events to subscribe to.
        @param envelopeHandler Function that will handle the message
                               envelope when it is received.
        @param queueName [optional] specify the named queue to bind
                                    the handler to.
    */

    EventManager.prototype.subscribeToEnvelope = function(eventSet, envelopeHandler, queueName) {
      return onEnvelope(eventSet, envelopeHandler, queueName);
    };

    return EventManager;

  })();

}).call(this);
