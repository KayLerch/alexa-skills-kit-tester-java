package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import io.klerch.alexa.tester.client.AlexaTestActor;

import java.util.*;

public class AlexaLaunchRequest extends AlexaRequest {
    public AlexaLaunchRequest(final AlexaTestActor actor) {
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
