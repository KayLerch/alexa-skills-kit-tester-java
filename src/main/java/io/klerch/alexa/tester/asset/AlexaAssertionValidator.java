package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import io.klerch.alexa.tester.response.AlexaResponse;

public interface AlexaAssertionValidator {
    boolean isTrue(final SpeechletResponseEnvelope response);
}
