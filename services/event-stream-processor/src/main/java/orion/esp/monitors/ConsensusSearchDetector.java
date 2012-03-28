package orion.esp.monitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import orion.esp.EnvelopeUtils;
import orion.esp.EventMonitor;
import orion.esp.EventStreamProcessor;
import orion.esp.InferredEvent;
import orion.esp.InferredEventList;
import orion.esp.publish.Publisher;

import pegasus.eventbus.client.Envelope;

import com.espertech.esper.client.EventBean;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

class ConsensusSearchDetector extends EventMonitor {

//    private static final String SEARCH = "Search";
    private static final String SEARCH = "pegasus.core.search.event.TextSearchEvent";
    public static final String INFERRED_TYPE = "ConsensusSearchEvent";
    private static final String TERM_KEY = "term";
    private static final String USER_KEY = "user";
    private static final String FREQ_KEY = "frequency";

    /**
     * Catch a search event and split it into multiple search term events. This normalizes case to lower-case and removes stop words.
     * 
     * e.g. when there's a event like Search("JackRyan", "Europe Satellite Imagery For Sunday") this class will generate the events SearchTerm("JackRyan", 'europe') SearchTerm("JackRyan", 'satellite')
     * SearchTerm("JackRyan", 'imagery') SearchTerm("JackRyan", 'sunday')
     * 
     * @author israel
     * 
     */
    class TermSplitter extends EventMonitor {

        boolean isEPL = true;
        String pattern = "select search from Envelope as search where eventType = 'Search'";
        private String fieldToSplit;
        private String inferredType;

        public TermSplitter(boolean isEPL, String pattern, String fieldToSplit, String inferredType) {
            super();
            this.inferredType = inferredType;
            this.isEPL = isEPL;
            this.pattern = pattern;
            this.fieldToSplit = fieldToSplit;
        }

        public InferredEventList receive(EventBean eventBean) {
            Envelope env = (Envelope) eventBean.get(fieldToSplit);
            InferredEventList events = new InferredEventList();
            String userid = env.getReplyTo();
            String topic = env.getTopic();
            Iterable<String> searchTerms = EnvelopeUtils.getSearchTerms(topic);
            for (String term : searchTerms) {
                InferredEvent event = makeInferredEvent();
                event.putData(USER_KEY, userid).putData(TERM_KEY, term);
                events.addInferredEvent(event);
            }
            return events;
        }


        @Override
        public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {
            esp.monitor(isEPL, pattern, this);

            // @todo = this needs to be integrated
            return new HashSet<Publisher>();
        }

        @Override
        public String getInferredType() {
            return inferredType;
        }
    }

    private int minFreq;
    private String timeLimit;

    public ConsensusSearchDetector(int minFreq, String timeLimit) {
        super();
        this.minFreq = minFreq;
        this.timeLimit = timeLimit;
    }

    public InferredEvent receive(EventBean eventBean) {
        InferredEvent event = makeInferredEvent();
        String term = (String) eventBean.get("term");
        Long freq = (Long) eventBean.get("freq");
        event.putData(FREQ_KEY, "" + freq).putData(TERM_KEY, term);
        return event;
    }

    @Override
    public Collection<Publisher> registerPatterns(EventStreamProcessor esp) {

        // To calculate search term frequency, we need to do the following:
        //
        // 1. split the search string into individual terms and add them individually
        // into the event stream (the TermSplitter class does this).
        // 2. extract the term and user strings from the search so that they can be aggregated
        // and filtered.
        // 3. calculate a frequency count aggregating by the term.
        // 4. filter the terms with q frequency count at least the minimumn specified
        // frequency count.

        String findSearches = "select search from Envelope as search where eventType = '" +
        		SEARCH +
        		"'";
        EventMonitor termSplitter = new TermSplitter(true, findSearches, "search", "Search Term");

        String createUT = "insert into UserTerms " + "select event.getData('" + TERM_KEY + "') as term, event.getData('" + USER_KEY + "') as user from InferredEvent.win:time(" + timeLimit
                + ") as event where type='Search Term'";

        // TODO: have frequency counts based on unique user requests

        String createSTF = "insert into SearchTermFreq select term, count(*) as freq " + "from UserTerms.win:time(" + timeLimit + ") group by term";

        String getSTF = "select * from SearchTermFreq.win:time(" + timeLimit + ") where freq >= " + minFreq;

        // extract search terms and add multiple inferred events (one for each)
        esp.monitor(true, findSearches, termSplitter);

        // extract out the search term and and user from the search term stream within the
        // time period
        esp.monitor(true, createUT, null);

        // extract frequency count by search term
        esp.monitor(true, createSTF, null);

        // filter out search terms with at least the desired minimum frequency
        esp.monitor(true, getSTF, this);

        // TODO: enable for multiple instantiations
        // this will run into problems if multiple CSDs are created; e.g.
        // CSD(20, 30min), CSD(30, 60min) since there will be multiple intermediate
        // events created (Search Terms, UserTerms, SearchTermFreq)

        // @todo = this needs to be integrated
        return new HashSet<Publisher>();
    }

    @Override
    public String getInferredType() {
        return INFERRED_TYPE;
    }

}
