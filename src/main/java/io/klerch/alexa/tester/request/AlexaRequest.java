package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.client.AlexaTestActor;
import java.util.UUID;

public abstract class AlexaRequest {
    private final ObjectMapper mapper;
    final AlexaTestActor actor;
    CoreSpeechletRequest speechletRequest;

    AlexaRequest(final AlexaTestActor actor) {
        this.actor = actor;
        this.mapper = new ObjectMapper();
    }

    String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public AlexaTestActor getActor() {
        return this.actor;
    }

    public abstract CoreSpeechletRequest getSpeechletRequest();

    public abstract boolean expectsResponse();
}
