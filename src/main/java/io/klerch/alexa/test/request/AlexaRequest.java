package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.actor.AlexaSessionActor;

import java.util.UUID;

public abstract class AlexaRequest {
    private final ObjectMapper mapper;
    final AlexaSessionActor actor;
    CoreSpeechletRequest speechletRequest;

    AlexaRequest(final AlexaSessionActor actor) {
        this.actor = actor;
        this.mapper = new ObjectMapper();
    }

    public static String generateRequestId() {
        return String.format("EdwRequestId.%s", UUID.randomUUID());
    }

    public AlexaSessionActor getActor() {
        return this.actor;
    }

    public abstract CoreSpeechletRequest getSpeechletRequest();

    public abstract boolean expectsResponse();
}
