package orion.esp;


public interface EventMonitorRepository {

    void registerWith(EventStreamProcessor eventStreamProcessor);

}
