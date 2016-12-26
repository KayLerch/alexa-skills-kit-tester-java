package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;

public interface AlexaAssertionValidator {
    boolean isTrue(final SpeechletResponseEnvelope response);
}
