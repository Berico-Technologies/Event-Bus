package gov.ment.esp.publish;

import java.util.Map;

public interface DataProvider {

  public static final String LABEL = "label";
  public static final String VALUE = "value";
  public static final String INFO = "info";
  public static final String TREND = "trend";
  public static final String METRICS = "metrics";

  public Map<String, Object> getData();
}
