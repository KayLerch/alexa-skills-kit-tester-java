package io.klerch.alexa.test.client.endpoint;

import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;

import java.util.Optional;

public interface AlexaEndpoint {
    Optional<AlexaResponse> fire(final AlexaRequest request, final String payload);
}
