package pegasus.esp;


import pegasus.eventbus.client.Envelope;


public class TestUtils {

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
        Envelope env = EnvelopeUtils.makeEnvelope("Search", reqID, null, resource, user);
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

