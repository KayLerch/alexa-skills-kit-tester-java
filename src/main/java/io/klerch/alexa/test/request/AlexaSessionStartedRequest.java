package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import io.klerch.alexa.test.actor.AlexaSessionActor;

import java.util.*;

public class AlexaSessionStartedRequest extends AlexaRequest {
    public AlexaSessionStartedRequest(final AlexaSessionActor actor) {
        super(actor);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    @Override
    public CoreSpeechletRequest getSpeechletRequest() {
        return SessionStartedRequest.builder()
                .withLocale(actor.getClient().getLocale())
                .withTimestamp(new Date())
                .withRequestId(generateRequestId())
                .build();
    }
}
