package gov.ment.esp;

public interface EventMonitorRepository {

  void registerWith(EventStreamProcessor eventStreamProcessor);

}
