package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.actor.AlexaActor;

import java.util.UUID;

public abstract class AlexaRequest {
    private final ObjectMapper mapper;
    final AlexaActor actor;
    CoreSpeechletRequest speechletRequest;

    AlexaRequest(final AlexaActor actor) {
        this.actor = actor;
        this.mapper = new ObjectMapper();
    }

    String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public AlexaActor getActor() {
        return this.actor;
    }

    public abstract CoreSpeechletRequest getSpeechletRequest();

    public abstract boolean expectsResponse();
}
