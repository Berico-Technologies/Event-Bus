package pegasus.esp;


public interface EventMonitorRepository {

    void registerWith(EventStreamProcessor eventStreamProcessor);

}
