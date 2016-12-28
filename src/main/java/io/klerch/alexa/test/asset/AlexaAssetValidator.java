package io.klerch.alexa.test.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;

public interface AlexaAssetValidator {
    boolean exists(final SpeechletResponseEnvelope response);
    boolean equals(final SpeechletResponseEnvelope response, final Object value);
    boolean matches(final SpeechletResponseEnvelope response, final String pattern);
}
