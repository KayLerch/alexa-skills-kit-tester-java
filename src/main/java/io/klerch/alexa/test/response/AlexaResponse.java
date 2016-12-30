package io.klerch.alexa.test.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.client.AlexaSessionActor;
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

/**
 * The response object holds the skill speechlet response and provides a bunch
 * of methods you can use to validate contents over assertions.
 */
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

    /**
     * An AlexaResponse is empty for requests that do not get a response from the skill
     * e.g. the SessionStartedRequest.
     * @return True, if there wasn't a response.
     */
    public boolean isEmpty() {
        return request == null;
    }

    /**
     * Validates the execution time of the request call to the skill. It
     * throws an IllegalArgumentException in case the execution time exceeded the
     * milliseconds given
     * @param millis milliseconds (inclusive) that should not be exceeded by the skill call
     * @return this response
     */
    public AlexaResponse assertExecutionTimeLessThan(final long millis) {
        final String assertionText = String.format("Execution is not longer than %s ms.", millis);
        final long executionMillis = request.getActor().getClient().getLastExecutionMillis();
        Validate.inclusiveBetween(0L, millis, executionMillis, "[FAILED] Assertion '%1Ss' is FALSE. Was %2Ss ms.", assertionText, executionMillis);
        log.debug(String.format("→ [PASSED] %s", assertionText));
        return this;
    }

    /**
     * Validates a custom predicate executed against the response envelope. It
     * throws an IllegalArgumentException in case the predicate is not true.
     * @param responseEnvelope the response envelope of the request
     * @return this response
     */
    public AlexaResponse assertThat(final Predicate<SpeechletResponseEnvelope> responseEnvelope) {
        final String assertionText = "Custom predicate matches.";
        return validate(is(responseEnvelope), assertionText);
    }

    /**
     * Validates a custom predicate executed against the response envelope.
     * @param responseEnvelope the response envelope of the request
     * @return True, if predicate is true
     */
    public boolean is(final Predicate<SpeechletResponseEnvelope> responseEnvelope) {
        return responseEnvelope.test(envelope);
    }

    /**
     * Validates the given assertion. It
     * throws an IllegalArgumentException in case the assertion is not true.
     * @param assertion The assertion
     * @return True, if assertion is true
     */
    public AlexaResponse assertTrue(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is TRUE.", assertion.name());
        return validate(isTrue(assertion), assertionText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @return True, if assertion is true
     */
    public boolean isTrue(final AlexaAssertion assertion) {
        return assertion.isTrue(envelope);
    }

    /**
     * Validates the given assertion. It
     * throws an IllegalArgumentException in case the assertion is not false.
     * @param assertion The assertion
     * @return this response
     */
    public AlexaResponse assertFalse(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is NOT true.", assertion.name());
        return validate(isFalse(assertion), assertionText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @return True, if assertion is false
     */
    public boolean isFalse(final AlexaAssertion assertion) {
        return !isTrue(assertion);
    }

    /**
     * Validates the existence of a speechlet asset inside the response. It
     * throws an IllegalArgumentException in case the asset does not exist.
     * @param asset asset to check for existence
     * @return this response
     */
    public AlexaResponse assertExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s exists.", asset.name());
        return validate(exists(asset), assertionText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @return True, if the asset exists
     */
    public boolean exists(final AlexaAsset asset) {
        return asset.exists(envelope);
    }

    /**
     * Validates the existence of a speechlet asset inside the response. It
     * throws an IllegalArgumentException in case the asset does exist.
     * @param asset asset to check for existence
     * @return this response
     */
    public AlexaResponse assertNotExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s does NOT exist.", asset.name());
        return validate(notExists(asset), assertionText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @return True, if the asset does not exist
     */
    public boolean notExists(final AlexaAsset asset) {
        return !exists(asset);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value. It
     * throws an IllegalArgumentException in case the asset has not the value given.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return this response
     */
    public AlexaResponse assertEquals(final AlexaAsset asset, final Object value){
        final String assertionText = String.format("%1$s is equal to '%2$s'.", asset.name(), value);
        return validate(equals(asset, value), assertionText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return True, if the asset has the value given
     */
    public boolean equals(final AlexaAsset asset, final Object value) {
        return asset.equals(envelope, value);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value. It
     * throws an IllegalArgumentException in case the asset has the value given.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return this response
     */
    public AlexaResponse assertNotEquals(final AlexaAsset asset, final Object value){
        final String assertionText = String.format("%1$s is NOT equal to '%2$s'.", asset.name(), value);
        return validate(notEquals(asset, value), assertionText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return True, if the asset has not the value given
     */
    public boolean notEquals(final AlexaAsset asset, final Object value) {
        return !equals(asset, value);
    }

    /**
     * Validates if the value of a speechlet asset matches the pattern given. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value. It
     * throws an IllegalArgumentException in case value of the asset does not match the pattern given.
     * @param asset the asset whose value needs to be checked
     * @param pattern regular expression
     * @return this response
     */
    public AlexaResponse assertMatches(final AlexaAsset asset, final String pattern){
        final String assertionText = String.format("%1$s matches pattern '%2$s'.", asset.name(), pattern);
        return validate(matches(asset, pattern), assertionText);
    }

    /**
     * Validates if the value of a speechlet asset matches the pattern given. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset the asset whose value needs to be checked
     * @param pattern regular expression
     * @return True, if the value matches the pattern given.
     */
    public boolean matches(final AlexaAsset asset, final String pattern) {
        return asset.matches(envelope, pattern);
    }

    /**
     * Validates if shouldEndSession is true. It
     * throws an IllegalArgumentException in case shouldEndSession is false.
     * @return this response
     */
    public AlexaResponse assertSessionEnded(){
        return assertTrue(AlexaAssertion.SessionEnded);
    }

    /**
     * Validates if shouldEndSession is true. It
     * throws an IllegalArgumentException in case shouldEndSession is false.
     * @return True, if shouldEndSession is true
     */
    public boolean sessionEnded() {
        return isTrue(AlexaAssertion.SessionEnded);
    }

    /**
     * Validates if shouldEndSession is false. It
     * throws an IllegalArgumentException in case shouldEndSession is true.
     * @return this response
     */
    public AlexaResponse assertSessionStillOpen(){
        return assertTrue(AlexaAssertion.SessionStillOpen);
    }

    /**
     * Validates if shouldEndSession is false. It
     * throws an IllegalArgumentException in case shouldEndSession is true.
     * @return True, if shouldEndSession is false
     */
    public boolean sessionOpen() {
        return isTrue(AlexaAssertion.SessionStillOpen);
    }

    /**
     * @return session attributes that came in with the last response and that will
     * be send with the next request.
     */
    public Map<String, Object> getSessionAttributes() {
        return envelope.getSessionAttributes();
    }

    /**
     * Validates if a session attribute exists. It throws an IllegalArgumentException
     * in case the session attribute does not exist.
     * @param key key of the session attribute
     * @return this response
     */
    public AlexaResponse assertSessionStateExists(final String key){
        final String assertionText = String.format("Session state with key '%1$s' exists.", key);
        return validate(sessionStateExists(key), assertionText);
    }

    /**
     * Validates if a session attribute exists.
     * @param key the key of the session attribute.
     * @return True, if session attribute with given key exists.
     */
    public boolean sessionStateExists(final String key) {
        return envelope.getSessionAttributes().containsKey(key);
    }

    /**
     * Validates if a session attributes value is not null. It throws an IllegalArgumentException
     * in case the session attribute is null.
     * @param key key of the session attribute.
     * @return this response
     */
    public AlexaResponse assertSessionStateNotNull(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT null.", key);
        return validate(sessionStateNotNull(key), assertionText);
    }

    /**
     * Validates if a session attributes value is not null.
     * @param key key of the session attribute.
     * @return True, if session attributes value with given key is not null
     */
    public boolean sessionStateNotNull(final String key) {
        return getSlotValue(key).orElse(null) != null;
    }

    /**
     * Validates if a session attributes value is not blank. It throws an IllegalArgumentException
     * in case the session attribute is blank.
     * @param key key of the session attribute.
     * @return this response
     */
    public AlexaResponse assertSessionStateNotBlank(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT blank.", key);
        return validate(sessionStateNotBlank(key), assertionText);
    }

    /**
     * Validates if a session attributes value is not blank.
     * @param key key of the session attribute.
     * @return True, if session attributes value with given key is not blank
     */
    public boolean sessionStateNotBlank(final String key) {
        return sessionStateNotNull(key) && StringUtils.isNotBlank(String.valueOf(getSlotValue(key).orElse("")));
    }

    /**
     * Validates if a session attributes value is equal to the value given. It throws an IllegalArgumentException
     * in case the session attributes value is not equal to the one given.
     * @param key key of the session attribute.
     * @param value expected value of the session attribute.
     * @return this response
     */
    public AlexaResponse assertSessionStateEquals(final String key, final String value){
        final String assertionText = String.format("Session state with key '%1$s' is equal to '%2$s'.", key, value);
        return validate(sessionStateEquals(key, value), assertionText);
    }

    /**
     * Validates if a session attributes value is equal to the value given.
     * @param key key of the session attribute.
     * @param value expected value of the session attribute.
     * @return True, if the session attributes value equals the value given
     */
    public boolean sessionStateEquals(final String key, final String value) {
        if (value != null) {
            return sessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).equals(value);
        } else {
            return envelope.getSessionAttributes().getOrDefault(key, null) == null;
        }
    }

    /**
     * Validates if a session attributes value contains the string given. It throws an IllegalArgumentException
     * in case the session attributes value does not contain the string given.
     * @param key key of the session attribute.
     * @param subString expected string portion in session attribute value.
     * @return this response
     */
    public AlexaResponse assertSessionStateContains(final String key, final String subString){
        final String assertionText = String.format("Session state with key '%1$s' contains '%2$s'.", key, subString);
        return validate(sessionStateContains(key, subString), assertionText);
    }

    /**
     * Validates if a session attributes value contains the string given.
     * @param key key of the session attribute.
     * @param subString expected string portion in session attribute value.
     * @return True, if the session attribute value contains the given string.
     */
    public boolean sessionStateContains(final String key, final String subString) {
        return sessionStateNotNull(key) && StringUtils.contains(String.valueOf(getSlotValue(key).orElse("")), subString);
    }

    /**
     * Validates if value of a session attribute matches the pattern given. It throws an IllegalArgumentException
     * in case the session attributes value does not match with the pattern given.
     * @param key key of the session attribute.
     * @param regex regular expression
     * @return this response
     */
    public AlexaResponse assertSessionStateMatches(final String key, final String regex){
        final String assertionText = String.format("Session state with key '%1$s' matches pattern '%2$s'.", key, regex);
        return validate(sessionStateMatches(key, regex), assertionText);
    }

    /**
     * Validates if value of a session attribute matches the pattern given.
     * @param key key of the session attribute.
     * @param regex regular expression
     * @return True, if the value matches the pattern given.
     */
    public boolean sessionStateMatches(final String key, final String regex) {
        return sessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).matches(regex);
    }

    /**
     * exits the fluent interface and returns the actor in order to continue with
     * next skill request.
     * @return actor
     */
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
