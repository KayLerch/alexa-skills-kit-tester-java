package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import io.klerch.alexa.test.actor.AlexaSessionActor;

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
    public CoreSpeechletRequest getSpeechletRequest() {
        return LaunchRequest.builder()
                .withLocale(actor.getClient().getLocale())
                .withRequestId(generateRequestId())
                .withTimestamp(new Date())
                .build();
    }
}
