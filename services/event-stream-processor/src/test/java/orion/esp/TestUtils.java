package orion.esp;


import pegasus.eventbus.client.Envelope;


public class TestUtils {

	private static final String SEARCH_EVENT = "pegasus.core.search.event.TextSearchEvent";

    public static Envelope makeRequest(String requestReferenceID) {
        return EnvelopeUtils.makeEnvelope("Request", requestReferenceID, null,
                "Request topic for " + requestReferenceID, "requester");
    }

    public static Envelope makeResponse(String responseReferenceID) {
        return EnvelopeUtils.makeEnvelope("Response", null, responseReferenceID,
                "Response topic for " + responseReferenceID, "responder");
    }

    public static Envelope makeAuthRequest(String user, String resource, String reqID) {
        Envelope env = EnvelopeUtils.makeEnvelope("Request", reqID, null, resource, user);
        return env;
    }

    public static Envelope makeAuthResponse(boolean allowed, String reqID) {
        String approval = allowed ? "APPROVED" : "Unauthorized Access";
        Envelope env = EnvelopeUtils.makeEnvelope("Response", null, reqID, approval, "Resource Allocation Server");
        return env;
    }

    public static Envelope makeSearchRequest(String user, String resource, String reqID) {
        String typex = SEARCH_EVENT;
        String old_search = "Search";
        Envelope env = EnvelopeUtils.makeEnvelope(SEARCH_EVENT, reqID, null, resource, user);
        return env;
    }

    public static Envelope createDocumentCollection(String reqID) {
        return EnvelopeUtils.makeEnvelope("DocumentCollectionSearchResult", null, reqID,
                reqID + " documents", "librarian");
    }

    public static Envelope createHitFrequency(String reqID) {
        return EnvelopeUtils.makeEnvelope("HitFrequencySearchResult", null, reqID,
                reqID + " hit frequency", "hit freq counter");
    }
}

