package io.klerch.alexa.tester.actor;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import io.klerch.alexa.tester.client.AlexaClient;
import io.klerch.alexa.tester.request.AlexaRequest;
import io.klerch.alexa.tester.response.AlexaResponse;

public abstract class AlexaActor {
    final AlexaClient client;

    public AlexaActor(final AlexaClient client) {
        this.client = client;
    }

    public AlexaClient getClient() {
        return this.client;
    }

    public abstract SpeechletRequestEnvelope envelope(final AlexaRequest request);

    public abstract void exploitResponse(final AlexaResponse response);
}
