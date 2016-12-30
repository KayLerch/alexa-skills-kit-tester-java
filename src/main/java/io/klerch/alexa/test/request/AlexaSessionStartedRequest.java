package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSessionActor;

public class AlexaSessionStartedRequest extends AlexaRequest {
    public AlexaSessionStartedRequest(final AlexaSessionActor actor) {
        super(actor);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return SessionStartedRequest.builder()
                .withLocale(actor.getClient().getLocale())
                .withTimestamp(actor.getClient().getCurrentTimestamp())
                .withRequestId(generateRequestId())
                .build();
    }
}
