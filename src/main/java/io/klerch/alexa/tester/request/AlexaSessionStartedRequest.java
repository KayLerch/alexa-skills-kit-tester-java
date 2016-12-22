package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import io.klerch.alexa.tester.client.AlexaTestActor;

import java.util.*;

public class AlexaSessionStartedRequest extends AlexaRequest {
    public AlexaSessionStartedRequest(final AlexaTestActor actor) {
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
