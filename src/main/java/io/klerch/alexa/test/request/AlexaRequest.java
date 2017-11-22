package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SpeechletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.client.AlexaSession;

import java.util.UUID;

public abstract class AlexaRequest {
    private final ObjectMapper mapper;
    final AlexaSession session;
    SpeechletRequest speechletRequest;

    AlexaRequest(final AlexaSession session) {
        this.session = session;
        this.mapper = new ObjectMapper();
    }

    public static String generateRequestId() {
        return String.format("EdwRequestId.%s", UUID.randomUUID());
    }

    public AlexaSession getSession() {
        return this.session;
    }

    public abstract SpeechletRequest getSpeechletRequest();

    public abstract boolean expectsResponse();
}
