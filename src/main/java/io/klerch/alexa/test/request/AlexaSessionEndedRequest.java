package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.test.actor.AlexaSessionActor;

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
    public CoreSpeechletRequest getSpeechletRequest() {
        return SessionEndedRequest.builder()
                .withRequestId(generateRequestId())
                .withTimestamp(new Date())
                .withReason(reason)
                .withLocale(actor.getClient().getLocale())
                .build();
    }
}
