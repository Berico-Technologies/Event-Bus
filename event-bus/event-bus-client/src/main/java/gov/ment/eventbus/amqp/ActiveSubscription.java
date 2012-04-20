package gov.ment.eventbus.amqp;

/**
 * Represents a Subscription with an active queue listener, pulling messages
 * from the Bus.
 * 
 * @author Ken Baltrinic (Berico Technologies)
 */
class ActiveSubscription {

  private final String queueName;
  private final Boolean queueIsDurable;
  final QueueListener listener;
  private boolean isActive = true;

  /**
   * Instantiate the class supplying the queue information and listener
   * 
   * @param queueName
   *          Name of the Queue being watched
   * @param queueIsDurable
   *          Is the Queue durable?
   * @param listener
   *          The listener watching for events
   */
  public ActiveSubscription(String queueName, Boolean queueIsDurable, QueueListener listener) {

    this.queueName = queueName;
    this.queueIsDurable = queueIsDurable;
    this.listener = listener;
  }

  /**
   * Get the Queue Name
   * 
   * @return Queue Name
   */
  public String getQueueName() {
    return queueName;
  }

  /**
   * Is the Queue Durable?
   * 
   * @return true if it is durable
   */
  public Boolean getQueueIsDurable() {
    return queueIsDurable;
  }

  /**
   * Get the Listener watching the Queue
   * 
   * @return Listener
   */
  public QueueListener getListener() {
    return listener;
  }

  /**
   * Is the subscription currently Active?
   * 
   * @return true if active.
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * Toggle whether the queue is active
   * 
   * @param queueIsDeleted
   *          Has the queue been deleted?
   */
  public void setIsActive(boolean queueIsDeleted) {
    this.isActive = queueIsDeleted;
  }

}
