package io.klerch.alexa.test.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.actor.AlexaSessionActor;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.request.AlexaRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AlexaResponse {
    private final static Logger log = Logger.getLogger(AlexaResponse.class);
    final SpeechletResponseEnvelope envelope;
    final AlexaRequest request;

    public AlexaResponse(final AlexaRequest request, final byte[] payload) {
        this.request = request;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            envelope = mapper.readValue(payload, SpeechletResponseEnvelope.class);
        } catch (final IOException e) {
            throw new RuntimeException("Invalid response format from Lambda function.", e);
        }
    }

    public boolean isEmpty() {
        return request == null;
    }

    public AlexaResponse assertExecutionTimeLessThan(final long millis) {
        final String assertionText = String.format("Execution is not longer than %s ms.", millis);
        final long executionMillis = request.getActor().getClient().getLastExecutionMillis();
        Validate.inclusiveBetween(0L, millis, executionMillis, "[FAILED] Assertion '%1Ss' is FALSE. Was %2Ss ms.", assertionText, executionMillis);
        log.debug(String.format("→ [PASSED] %s", assertionText));
        return this;
    }

    public AlexaResponse assertThat(final Predicate<SpeechletResponseEnvelope> responseEnvelope) {
        final String assertionText = "Custom predicate matches.";
        return validate(is(responseEnvelope), assertionText);
    }

    public boolean is(final Predicate<SpeechletResponseEnvelope> responseEnvelope) {
        return responseEnvelope.test(envelope);
    }

    public AlexaResponse assertTrue(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is TRUE.", assertion.name());
        return validate(isTrue(assertion), assertionText);
    }

    public boolean isTrue(final AlexaAssertion assertion) {
        return assertion.isTrue(envelope);
    }

    public AlexaResponse assertFalse(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is NOT true.", assertion.name());
        return validate(isFalse(assertion), assertionText);
    }

    public boolean isFalse(final AlexaAssertion assertion) {
        return !isTrue(assertion);
    }

    public AlexaResponse assertExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s exists.", asset.name());
        return validate(exists(asset), assertionText);
    }

    public boolean exists(final AlexaAsset asset) {
        return asset.exists(envelope);
    }

    public AlexaResponse assertNotExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s does NOT exist.", asset.name());
        return validate(notExists(asset), assertionText);
    }

    public boolean notExists(final AlexaAsset asset) {
        return !exists(asset);
    }

    public AlexaResponse assertEquals(final AlexaAsset asset, final Object value){
        final String assertionText = String.format("'%1$s' is equal to '%2$s'.", asset.name(), value);
        return validate(equals(asset, value), assertionText);
    }

    public boolean equals(final AlexaAsset asset, final Object value) {
        return asset.equals(envelope, value);
    }

    public AlexaResponse assertNotEquals(final AlexaAsset asset, final Object value){
        final String assertionText = String.format("'%1$s' is NOT equal to '%2$s'.", asset.name(), value);
        return validate(notEquals(asset, value), assertionText);
    }

    public boolean notEquals(final AlexaAsset asset, final Object value) {
        return !equals(asset, value);
    }

    public AlexaResponse assertMatches(final AlexaAsset asset, final String pattern){
        final String assertionText = String.format("'%1$s' matches pattern '%2$s'.", asset.name(), pattern);
        return validate(matches(asset, pattern), assertionText);
    }

    public boolean matches(final AlexaAsset asset, final String pattern) {
        return asset.matches(envelope, pattern);
    }

    public AlexaResponse assertSessionEnded(){
        return assertTrue(AlexaAssertion.SessionEnded);
    }

    public boolean sessionEnded() {
        return isTrue(AlexaAssertion.SessionEnded);
    }

    public AlexaResponse assertSessionStillOpen(){
        return assertTrue(AlexaAssertion.SessionStillOpen);
    }

    public boolean sessionOpen() {
        return isTrue(AlexaAssertion.SessionStillOpen);
    }

    public Map<String, Object> getSessionAttributes() {
        return envelope.getSessionAttributes();
    }

    public AlexaResponse assertSessionStateExists(final String key){
        final String assertionText = String.format("Session state with key '%1$s' exists.", key);
        return validate(sessionStateExists(key), assertionText);
    }

    public boolean sessionStateExists(final String key) {
        return envelope.getSessionAttributes().containsKey(key);
    }

    public AlexaResponse assertSessionStateNotNull(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT null.", key);
        return validate(sessionStateNotNull(key), assertionText);
    }

    public boolean sessionStateNotNull(final String key) {
        return getSlotValue(key).orElse(null) != null;
    }

    public AlexaResponse assertSessionStateNotBlank(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT blank.", key);
        return validate(sessionStateNotBlank(key), assertionText);
    }

    public boolean sessionStateNotBlank(final String key) {
        return sessionStateNotNull(key) && StringUtils.isNotBlank(String.valueOf(getSlotValue(key).orElse("")));
    }

    public AlexaResponse assertSessionStateEquals(final String key, final String value){
        final String assertionText = String.format("Session state with key '%1$s' is equal to '%2$s'.", key, value);
        return validate(sessionStateEquals(key, value), assertionText);
    }

    public boolean sessionStateEquals(final String key, final String value) {
        if (value != null) {
            return sessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).equals(value);
        } else {
            return envelope.getSessionAttributes().getOrDefault(key, null) == null;
        }
    }

    public AlexaResponse assertSessionStateContains(final String key, final String subString){
        final String assertionText = String.format("Session state with key '%1$s' contains '%2$s'.", key, subString);
        return validate(sessionStateContains(key, subString), assertionText);
    }

    public boolean sessionStateContains(final String key, final String subString) {
        return sessionStateNotNull(key) && StringUtils.contains(String.valueOf(getSlotValue(key).orElse("")), subString);
    }

    public AlexaResponse assertSessionStateMatches(final String key, final String regex){
        final String assertionText = String.format("Session state with key '%1$s' matches pattern '%2$s'.", key, regex);
        return validate(sessionStateMatches(key, regex), assertionText);
    }

    public boolean sessionStateMatches(final String key, final String regex) {
        return sessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).matches(regex);
    }

    public AlexaSessionActor done() {
        return request.getActor();
    }

    private AlexaResponse validate(final boolean assertionResult, final String assertionText) {
        Validate.isTrue(assertionResult, "[FAILED] Assertion '%1$s' is FALSE.", assertionText);
        log.debug(String.format("→ [PASSED] %s", assertionText));
        return this;
    }

    @SuppressWarnings("unchecked")
    private Optional<Object> getSlotValue(final String key) {
        if (envelope.getSessionAttributes().containsKey(key)) {
            final Object val = envelope.getSessionAttributes().get(key);
            return val == null ? Optional.empty() :
                    val instanceof LinkedHashMap ? Optional.of(((LinkedHashMap)val).getOrDefault("value", "")) :
                            Optional.of(val);
        }
        return Optional.empty();
    }
}
