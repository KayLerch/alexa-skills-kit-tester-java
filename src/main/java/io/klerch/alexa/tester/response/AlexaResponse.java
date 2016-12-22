package io.klerch.alexa.tester.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.client.AlexaTestActor;
import io.klerch.alexa.tester.asset.AlexaAssertion;
import io.klerch.alexa.tester.asset.AlexaAsset;
import io.klerch.alexa.tester.request.AlexaRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.Map;

public class AlexaResponse {
    public static AlexaResponse VOID = new AlexaResponse();
    private final SpeechletResponseEnvelope envelope;
    private final AlexaRequest request;

    public AlexaResponse(final AlexaRequest request, final byte[] payload) {
        this.request = request;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            envelope = mapper.readValue(payload, SpeechletResponseEnvelope.class);
        } catch (final IOException e) {
            throw new RuntimeException("Invalid response format from Lambda function.", e);
        }
    }

    private AlexaResponse() {
        this.request = null;
        this.envelope = null;
    }

    public boolean getShouldEndSession() {
        return envelope.getResponse().getShouldEndSession();
    }

    public Map<String, Object> getSessionAttributes() {
        return envelope.getSessionAttributes();
    }

    public AlexaResponse assertThat(final AlexaAssertion assertion) {
        return assertTrue(assertion);
    }

    public AlexaResponse assertTrue(final AlexaAssertion assertion){
        Validate.isTrue(assertion.isTrue(envelope), "Assertion '$1%s' is not true in $2%s", assertion.name(), request);
        return this;
    }

    public AlexaResponse assertFalse(final AlexaAssertion assertion){
        Validate.isTrue(!assertion.isTrue(envelope), "Assertion '$1%s' is true in $2%s", assertion.name(), request);
        return this;
    }

    public AlexaResponse assertExists(final AlexaAsset asset){
        Validate.isTrue(asset.exists(envelope), "Asset '$1%s' does not exist in $2%s", asset.name(), request);
        return this;
    }

    public AlexaResponse assertNotExists(final AlexaAsset asset){
        Validate.isTrue(!asset.exists(envelope), "Asset '$1%s' does not exist in $2%s", asset.name(), request);
        return this;
    }

    public AlexaResponse assertEquals(final AlexaAsset asset, final Object value){
        Validate.isTrue(asset.equals(envelope, value), "Asset '$1%s' is not equal to '$2%s' in $3%s", asset.name(), value, request);
        return this;
    }

    public AlexaResponse assertNotEquals(final AlexaAsset asset, final Object value){
        Validate.isTrue(!asset.equals(envelope, value), "Asset '$1%s' is not equal to '$2%s' in $3%s", asset.name(), value, request);
        return this;
    }

    public AlexaResponse assertSessionStateExists(final String key){
        Validate.isTrue(envelope.getSessionAttributes().containsKey(key), "Session state with key '$1%s' does not exist in $2%s", key, request);
        return this;
    }

    public AlexaResponse assertSessionStateNotNull(final String key){
        Validate.notNull(envelope.getSessionAttributes().get(key), "Session state with key '$1%s' is null in $2%s", key, request);
        return this;
    }

    public AlexaResponse assertSessionStateNotBlank(final String key){
        assertSessionStateNotNull(key);
        Validate.notBlank(envelope.getSessionAttributes().get(key).toString(), "Session state with key '$1%s' is blank in $2%s", key, request);
        return this;
    }

    public AlexaResponse assertSessionStateEquals(final String key, final Object value){
        if (value != null) {
            assertSessionStateNotNull(key);
            Validate.isTrue(envelope.getSessionAttributes().get(key).equals(value), "Session state with key '$1%s' is not equal to '$2%s' in $3%s", key, value, request);
        } else {
            Validate.isTrue(envelope.getSessionAttributes().getOrDefault(key, null) == null, "Session state with key '$1%s' is not null in $3%s", key, request);
        }
        return this;
    }

    public AlexaResponse assertSessionStateContains(final String key, final String subString){
        assertSessionStateNotNull(key);
        Validate.isTrue(StringUtils.contains(envelope.getSessionAttributes().get(key).toString(), subString), "Session state with key '$1%s' containing '$2%s' expected, but was not found in $3%s", key, subString, request);
        return this;
    }

    public AlexaResponse assertSessionStateMatches(final String key, final String regex){
        assertSessionStateNotNull(key);
        Validate.matchesPattern(envelope.getSessionAttributes().get(key).toString(), regex, "Session state with key '$1%s' matches pattern of '$2%s' expected, but did not match in $3%s", key, regex, request);
        return this;
    }

    public AlexaResponse assertSessionEnded(){
        Validate.isTrue(envelope.getResponse().getShouldEndSession(), "ShouldEndSession expected to be true, but was false in %s", request);
        return this;
    }

    public AlexaResponse assertSessionOpen(){
        Validate.isTrue(!envelope.getResponse().getShouldEndSession(), "ShouldEndSession expected to be false, but was true in %s", request);
        return this;
    }

    public AlexaTestActor done() {
        return request.getActor();
    }
}
