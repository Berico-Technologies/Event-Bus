
package pegasus.esp;

import java.util.Collection;
import java.util.HashSet;

import com.espertech.esper.client.EventBean;

class UnauthorizedAccessAttemptsDetector extends EventMonitor {

    public static final String INFERRED_TYPE = "UnauthorizedAccessAttempts";

    private int minFreq;
    private String timeLimit;

    public UnauthorizedAccessAttemptsDetector(int minFreq, String timeLimit) {
        super();
        this.minFreq = minFreq;
        this.timeLimit = timeLimit;
    }

    @Override
    public InferredEvent receive(EventBean eventBean) {
        String userid = (String) eventBean.get("userid");
        long freq = (Long) eventBean.get("freq");
        InferredEvent resultingEvent = makeInferredEvent();
        resultingEvent.putData("User", userid);
        resultingEvent.putData("Occurrences", "" + freq);
        resultingEvent.putData("Period", "" + timeLimit);
        return resultingEvent;
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {

        String ipattern = "every request=Envelope(eventType='Request')" +
                " -> response=Envelope(eventType='Response' and " +
                "correlationId=request.id and topic='Unauthorized Access')";

        String createUA = "insert into UnauthorizedAccesses " +
                "select request.replyTo as userid, request, response " +
                "from pattern [ " + ipattern +  " ]";

        String createUAF = "insert into UnauthorizedAccessesFreq " +
                "select userid, count(*) as freq from UnauthorizedAccesses.win:time(" +
                timeLimit + ") group by userid";
        String getUAF = "select * from UnauthorizedAccessesFreq.win:time(" + timeLimit +
                ") where freq >= " + minFreq;

        esp.monitor(true, createUA, null);
        esp.monitor(true, createUAF, null);
        esp.monitor(true, getUAF, this);

        // @todo = this needs to be integrated
        return new HashSet<Publisher>();
    }

    @Override
    public String getInferredType() {
        return INFERRED_TYPE;
    }

}
