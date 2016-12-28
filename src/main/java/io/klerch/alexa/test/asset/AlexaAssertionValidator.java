package io.klerch.alexa.test.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;

public interface AlexaAssertionValidator {
    boolean isTrue(final SpeechletResponseEnvelope response);
}
