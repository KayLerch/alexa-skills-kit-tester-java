package io.klerch.alexa.test.actor;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import io.klerch.alexa.test.client.AlexaClient;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;

/**
 * An actor manages the conversation with a skill and is created by the client.
 * The client is used by the actor to fire requests.
 */
public abstract class AlexaActor {
    final AlexaClient client;

    /**
     * A new actor that uses the client provided to have a conversation with a skill.
     * The client holds the strategy to communicate with the skill.
     * @param client the client to use
     */
    public AlexaActor(final AlexaClient client) {
        this.client = client;
    }

    /**
     * The client is used by the actor to fire requests. The client holds the strategy
     * to communicate with the skill.
     * @return client
     */
    public AlexaClient getClient() {
        return this.client;
    }

    /**
     * The method of enveloping a speechlet request is delegated to the actor as he is
     * the one able to decide how to do it.
     * @param request the request that needs an envelopment
     * @return the enveloped skill request
     */
    public abstract SpeechletRequestEnvelope envelope(final AlexaRequest request);

    /**
     * Once a request was made to the skill a response is received. The client will
     * hand over that response to the actor in order to let him exploit it.
     * @param response the response to exploit
     */
    public abstract void exploitResponse(final AlexaResponse response);
}
