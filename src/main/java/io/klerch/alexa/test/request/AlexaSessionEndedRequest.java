package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
import io.klerch.alexa.test.client.AlexaSessionActor;

import java.util.Date;

public class AlexaSessionEndedRequest extends AlexaRequest {
    final SessionEndedRequest.Reason reason;

    public AlexaSessionEndedRequest(final AlexaSessionActor actor) {
        this(actor, SessionEndedRequest.Reason.USER_INITIATED);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    public AlexaSessionEndedRequest(final AlexaSessionActor actor, final SessionEndedRequest.Reason reason) {
        super(actor);
        this.reason = reason;
    }

    @Override
    public SpeechletRequest getSpeechletRequest() {
        return SessionEndedRequest.builder()
                .withRequestId(generateRequestId())
                .withTimestamp(actor.getClient().getCurrentTimestamp())
                .withReason(reason)
                .withLocale(actor.getClient().getLocale())
                .build();
    }
}
