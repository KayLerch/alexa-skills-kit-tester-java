package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.tester.client.AlexaTestActor;

import java.util.Date;

public class AlexaSessionEndedRequest extends AlexaRequest {
    final SessionEndedRequest.Reason reason;

    public AlexaSessionEndedRequest(final AlexaTestActor actor) {
        this(actor, SessionEndedRequest.Reason.USER_INITIATED);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    public AlexaSessionEndedRequest(final AlexaTestActor actor, final SessionEndedRequest.Reason reason) {
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
