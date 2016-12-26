package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.actor.AlexaActor;
import io.klerch.alexa.tester.actor.AlexaSessionActor;

import java.util.UUID;

public abstract class AlexaRequest {
    private final ObjectMapper mapper;
    final AlexaSessionActor actor;
    CoreSpeechletRequest speechletRequest;

    AlexaRequest(final AlexaSessionActor actor) {
        this.actor = actor;
        this.mapper = new ObjectMapper();
    }

    String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public AlexaSessionActor getActor() {
        return this.actor;
    }

    public abstract CoreSpeechletRequest getSpeechletRequest();

    public abstract boolean expectsResponse();
}
