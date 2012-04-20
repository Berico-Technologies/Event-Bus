package gov.ment.eventbus.testsupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TestSendEvent {

  private static final Logger LOG = LoggerFactory.getLogger(TestSendEvent.class);

  protected String name;
  protected Integer count;
  protected List<String> excuses = new ArrayList<String>();
  protected Date time;
  protected UUID id = UUID.randomUUID();

  public TestSendEvent() {
  }

  public TestSendEvent(String name, Date time, Integer count, String... excuses) {
    this.name = name;
    this.time = time;
    this.count = count;
    for (String excuse : excuses)
      this.addExcuses(excuse);
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public List<String> getExcuses() {
    return excuses;
  }

  public void addExcuses(String excuse) {
    this.excuses.add(excuse);
  }

  public void assertIsEquevalentTo(TestSendEvent event) {
    assertEquals(id, event.getId());
    assertEquals(name, event.getName());
    assertEquals(count, event.getCount());
    assertEquals(time, event.getTime());
    assertArrayEquals(excuses.toArray(), event.getExcuses().toArray());
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
    builder.append("name", name);
    builder.append("time", time);
    builder.append("count", count);
    builder.append("excuses", excuses);
    return builder.toString();
  }

  public void handleEvent(Object event) {
    LOG.debug("\n*****************************************\n");
    LOG.debug("{}", event);
    LOG.debug("\n*****************************************\n");

  }
}
