package pegasus.esp;

import com.espertech.esper.client.EventBean;

public class Utils {

    public static String beanString(EventBean eventBean) {
        StringBuffer sb = new StringBuffer("EventBean[");
        String sep = "";
        for (String prop : eventBean.getEventType().getPropertyNames()) {
            sb.append(sep + prop + "=" + eventBean.get(prop));
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    }
}
