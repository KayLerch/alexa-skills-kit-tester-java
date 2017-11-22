package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSession;

public class AlexaLaunchRequest extends AlexaRequest {
    public AlexaLaunchRequest(final AlexaSession actor) {
        super(actor);
    }

    @Override
    public boolean expectsResponse() {
        return true;
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return LaunchRequest.builder()
                .withLocale(session.getClient().getLocale())
                .withRequestId(generateRequestId())
                .withTimestamp(session.getClient().getCurrentTimestamp())
                .build();
    }
}
