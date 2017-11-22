package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSession;

public class AlexaUtteranceRequest extends AlexaRequest {
    public AlexaUtteranceRequest(final AlexaSession actor) {
        super(actor);
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return null;
    }

    @Override
    public boolean expectsResponse() {
        return true;
    }
}
