package orion.esp.publish;

import java.util.Collection;

public interface PublishingService extends Broker {

    public void addPublisher(Publisher publisher);
    
    public void addPublishers(Collection<Publisher> publishers);

    public void start();

    public void stop();

    public void removePublishers(Collection<Publisher> publishers);
}
