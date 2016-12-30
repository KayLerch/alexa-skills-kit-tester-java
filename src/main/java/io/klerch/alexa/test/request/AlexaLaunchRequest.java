package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSessionActor;

import java.util.*;

public class AlexaLaunchRequest extends AlexaRequest {
    public AlexaLaunchRequest(final AlexaSessionActor actor) {
        super(actor);
    }

    @Override
    public boolean expectsResponse() {
        return true;
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return LaunchRequest.builder()
                .withLocale(actor.getClient().getLocale())
                .withRequestId(generateRequestId())
                .withTimestamp(actor.getClient().getCurrentTimestamp())
                .build();
    }
}
