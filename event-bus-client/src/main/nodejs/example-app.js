(function() {
  var config, em, eventManager, nb, nullbus;

  em = require('./event-manager.js');

  nb = require('./null-bus.js');

  nullbus = new nb.NullBus();

  config = {
    bus: nullbus
  };

  eventManager = new em.EventManager(config);

  eventManager.start();

  eventManager.close();

}).call(this);
