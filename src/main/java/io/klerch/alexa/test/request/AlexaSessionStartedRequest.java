package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSession;

public class AlexaSessionStartedRequest extends AlexaRequest {
    public AlexaSessionStartedRequest(final AlexaSession actor) {
        super(actor);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return SessionStartedRequest.builder()
                .withLocale(session.getClient().getLocale())
                .withTimestamp(session.getClient().getCurrentTimestamp())
                .withRequestId(generateRequestId())
                .build();
    }
}
