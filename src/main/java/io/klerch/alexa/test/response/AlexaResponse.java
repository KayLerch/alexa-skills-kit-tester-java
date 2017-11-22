package io.klerch.alexa.test.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import io.klerch.alexa.test.client.AlexaSession;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.request.AlexaRequest;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The response object holds the skill speechlet response and provides a bunch
 * of methods you can use to validate contents over assertions.
 */
public class AlexaResponse {
    private static final Configuration config = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST)
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();

    private final static Logger log = Logger.getLogger(AlexaResponse.class);
    final SpeechletResponseEnvelope envelope;
    final String responsePayload;
    final String requestPayload;
    final AlexaRequest request;

    public AlexaResponse(final AlexaRequest request, final String requestPayload, final String responsePayload) {
        this.request = request;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        final ObjectMapper mapper = new ObjectMapper();
        try {
            envelope = mapper.readValue(responsePayload, SpeechletResponseEnvelope.class);
        } catch (final IOException e) {
            throw new RuntimeException("Invalid response format from Lambda function.", e);
        }
    }

    public SpeechletResponseEnvelope getResponseEnvelope() {
        return this.envelope;
    }

    public AlexaRequest getRequest() {
        return this.request;
    }

    public Optional<String> get(String jsonPath) {
        if (!jsonPath.startsWith("$")) jsonPath = "$" + jsonPath;
        List<String> result = JsonPath.using(config).parse(responsePayload).read(jsonPath);

        if (result == null || result.isEmpty()) {
            result = JsonPath.using(config).parse(requestPayload).read(jsonPath);
        }

        return Optional.ofNullable(result)
                .filter(l -> !l.isEmpty())
                .map(l -> l.get(0));
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
        final long executionMillis = request.getSession().getClient().getLastExecutionMillis();
        Validate.inclusiveBetween(0L, millis, executionMillis, "[FAILED] Assertion '%1Ss' is FALSE. Was %2Ss ms.", assertionText, executionMillis);
        log.info(String.format("->[TRUE] %s", assertionText));
        return this;
    }

    /**
     * Validates a custom predicate executed against the response envelope. It
     * throws an IllegalArgumentException in case the predicate is not true.
     * @param responseEnvelope the response envelope of the request
     * @return this response
     */
    public AlexaResponse assertThat(final Predicate<SpeechletResponseEnvelope> responseEnvelope) {
        final String assertionText = "Custom predicate ifMatch.";
        return validate(is(responseEnvelope), assertionText);
    }

    /**
     * Validates a custom json-path expression
     * throws an IllegalArgumentException in the expression does not return an element
     * @param jsonPathExpression a json-path expression like "?(@.field == 2)"
     * @return this response
     */
    public AlexaResponse assertThat(final String jsonPathExpression) {
        return validate(is(jsonPathExpression), "JSON path expression " + jsonPathExpression);
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
     * Validates a custom json-path expression
     * @param jsonPathExpression a json-path expression like "?(@.field == 2)"
     * @return True, if expression returns an element
     */
    public boolean is(String jsonPathExpression) {
        final String conditionalText = String.format("%1$s is TRUE.", jsonPathExpression);

        if (!jsonPathExpression.startsWith("?(@.")) {
            jsonPathExpression = "?(@." + jsonPathExpression + ")";
        }
        final String jsonPath = "$.response[" + jsonPathExpression + "]";
        final Object o = JsonPath.using(config).parse("{ \"response\" : [ " + responsePayload + " ]}").read(jsonPath);
        return result(o != null && o instanceof JSONArray && !((JSONArray)o).isEmpty(), conditionalText);
    }

    /**
     * Validates a custom predicate executed against the response envelope.
     * @param responseEnvelope the response envelope of the request
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse is(final Predicate<SpeechletResponseEnvelope> responseEnvelope, final Consumer<AlexaSession> followUp) {
        if (is(responseEnvelope)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates the given assertion. It
     * throws an IllegalArgumentException in case the assertion is not true.
     * @param assertion The assertion
     * @return True, if assertion is true
     */
    public AlexaResponse assertTrue(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is TRUE.", assertion.name());
        return validate(ifTrue(assertion), assertionText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @return True, if assertion is true
     */
    public boolean ifTrue(final AlexaAssertion assertion) {
        final String conditionalText = String.format("%1$s is TRUE.", assertion.name());
        return result(assertion.isTrue(envelope), conditionalText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifTrue(final AlexaAssertion assertion, final Consumer<AlexaSession> followUp) {
        if (ifTrue(assertion)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates the given assertion. It
     * throws an IllegalArgumentException in case the assertion is not false.
     * @param assertion The assertion
     * @return this response
     */
    public AlexaResponse assertFalse(final AlexaAssertion assertion){
        final String assertionText = String.format("%1$s is NOT true.", assertion.name());
        return validate(ifFalse(assertion), assertionText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @return True, if assertion is false
     */
    public boolean ifFalse(final AlexaAssertion assertion) {
        final String conditionalText = String.format("%1$s is NOT true.", assertion.name());
        return result(!assertion.isTrue(envelope), conditionalText);
    }

    /**
     * Validates the given assertion.
     * @param assertion The assertion
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifFalse(final AlexaAssertion assertion, final Consumer<AlexaSession> followUp) {
        if (ifFalse(assertion)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates the existence of a speechlet asset inside the response. It
     * throws an IllegalArgumentException in case the asset does not exist.
     * @param asset asset to check for existence
     * @return this response
     */
    public AlexaResponse assertExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s ifExists.", asset.name());
        return validate(ifExists(asset), assertionText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @return True, if the asset ifExists
     */
    public boolean ifExists(final AlexaAsset asset) {
        final String conditionalText = String.format("%1$s ifExists.", asset.name());
        return result(asset.exists(envelope), conditionalText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifExists(final AlexaAsset asset, final Consumer<AlexaSession> followUp) {
        if (ifExists(asset)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates the existence of a speechlet asset inside the response. It
     * throws an IllegalArgumentException in case the asset does exist.
     * @param asset asset to check for existence
     * @return this response
     */
    public AlexaResponse assertNotExists(final AlexaAsset asset){
        final String assertionText = String.format("%1$s does NOT exist.", asset.name());
        return validate(ifNotExists(asset), assertionText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @return True, if the asset does not exist
     */
    public boolean ifNotExists(final AlexaAsset asset) {
        final String conditionalText = String.format("%1$s does NOT exist.", asset.name());
        return result(!asset.exists(envelope), conditionalText);
    }

    /**
     * Validates the existence of a speechlet asset inside the response.
     * @param asset asset to check for existence
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifNotExists(final AlexaAsset asset, final Consumer<AlexaSession> followUp) {
        if (ifNotExists(asset)) {
            followUp.accept(request.getSession());
        }
        return this;
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
        return validate(ifEquals(asset, value), assertionText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return True, if the asset has the value given
     */
    public boolean ifEquals(final AlexaAsset asset, final Object value) {
        final String conditionalText = String.format("%1$s is equal to '%2$s'.", asset.name(), value);
        return result(asset.equals(envelope, value), conditionalText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifEquals(final AlexaAsset asset, final Object value, final Consumer<AlexaSession> followUp) {
        if (ifEquals(asset, value)) {
            followUp.accept(request.getSession());
        }
        return this;
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
        return validate(ifNotEquals(asset, value), assertionText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @return True, if the asset has not the value given
     */
    public boolean ifNotEquals(final AlexaAsset asset, final Object value) {
        final String conditionalText = String.format("%1$s is NOT equal to '%2$s'.", asset.name(), value);
        return result(!asset.equals(envelope, value), conditionalText);
    }

    /**
     * Validates the value of a speechlet asset. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset asset whose value needs to be checked
     * @param value the expected value of the speechlet asset
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifNotEquals(final AlexaAsset asset, final Object value, final Consumer<AlexaSession> followUp) {
        if (ifNotEquals(asset, value)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if the value of a speechlet asset ifMatch the pattern given. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value. It
     * throws an IllegalArgumentException in case value of the asset does not match the pattern given.
     * @param asset the asset whose value needs to be checked
     * @param pattern regular expression
     * @return this response
     */
    public AlexaResponse assertMatches(final AlexaAsset asset, final String pattern){
        final String assertionText = String.format("%1$s ifMatch pattern '%2$s'.", asset.name(), pattern);
        return validate(ifMatch(asset, pattern), assertionText);
    }

    /**
     * Validates if the value of a speechlet asset ifMatch the pattern given. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset the asset whose value needs to be checked
     * @param pattern regular expression
     * @return True, if the value ifMatch the pattern given.
     */
    public boolean ifMatch(final AlexaAsset asset, final String pattern) {
        final String conditionalText = String.format("%1$s ifMatch pattern '%2$s'.", asset.name(), pattern);
        return result(asset.matches(envelope, pattern), conditionalText);
    }

    /**
     * Validates if the value of a speechlet asset ifMatch the pattern given. In case the asset is not a field but
     * a field group the entire JSON portion of that asset is treated as the value.
     * @param asset the asset whose value needs to be checked
     * @param pattern regular expression
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifMatch(final AlexaAsset asset, final String pattern, final Consumer<AlexaSession> followUp) {
        if (ifMatch(asset, pattern)) {
            followUp.accept(request.getSession());
        }
        return this;
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
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionEnded(final Consumer<AlexaSession> followUp) {
        if (ifSessionEnded()) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if shouldEndSession is true. It
     * throws an IllegalArgumentException in case shouldEndSession is false.
     * @return True, if shouldEndSession is true
     */
    public boolean ifSessionEnded() {
        return ifTrue(AlexaAssertion.SessionEnded);
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
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStillOpen(final Consumer<AlexaSession> followUp) {
        if (ifSessionStillOpen()) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if shouldEndSession is false. It
     * throws an IllegalArgumentException in case shouldEndSession is true.
     * @return True, if shouldEndSession is false
     */
    public boolean ifSessionStillOpen() {
        return ifTrue(AlexaAssertion.SessionStillOpen);
    }


    /**
     * Validates if a session attribute ifExists. It throws an IllegalArgumentException
     * in case the session attribute does not exist.
     * @param key key of the session attribute
     * @return this response
     */
    public AlexaResponse assertSessionStateExists(final String key){
        final String assertionText = String.format("Session state with key '%1$s' ifExists.", key);
        return validate(ifSessionStateExists(key), assertionText);
    }

    /**
     * Validates if a session attribute ifExists.
     * @param key the key of the session attribute.
     * @return True, if session attribute with given key ifExists.
     */
    public boolean ifSessionStateExists(final String key) {
        final String conditionalText = String.format("Session state with key '%1$s' ifExists.", key);
        return result(envelope.getSessionAttributes().containsKey(key), conditionalText);
    }

    /**
     * Validates if a session attribute ifExists.
     * @param key the key of the session attribute.
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateExists(final String key, final Consumer<AlexaSession> followUp) {
        if (ifSessionStateExists(key)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if a session attributes value is not null. It throws an IllegalArgumentException
     * in case the session attribute is null.
     * @param key key of the session attribute.
     * @return this response
     */
    public AlexaResponse assertSessionStateNotNull(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT null.", key);
        return validate(ifSessionStateNotNull(key), assertionText);
    }

    /**
     * Validates if a session attributes value is not null.
     * @param key key of the session attribute.
     * @return True, if session attributes value with given key is not null
     */
    public boolean ifSessionStateNotNull(final String key) {
        final String conditionalText = String.format("Session state with key '%1$s' is NOT null.", key);
        return result((getSlotValue(key).orElse(null) != null), conditionalText);
    }

    /**
     * Validates if a session attributes value is not null.
     * @param key key of the session attribute.
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateNotNull(final String key, Consumer<AlexaSession> followUp) {
        if (ifSessionStateNotNull(key)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if a session attributes value is not blank. It throws an IllegalArgumentException
     * in case the session attribute is blank.
     * @param key key of the session attribute.
     * @return this response
     */
    public AlexaResponse assertSessionStateNotBlank(final String key){
        final String assertionText = String.format("Session state with key '%1$s' is NOT blank.", key);
        return validate(ifSessionStateNotBlank(key), assertionText);
    }

    /**
     * Validates if a session attributes value is not blank.
     * @param key key of the session attribute.
     * @return True, if session attributes value with given key is not blank
     */
    public boolean ifSessionStateNotBlank(final String key) {
        final String conditionalText = String.format("Session state with key '%1$s' is NOT blank.", key);
        final boolean conditionalResult = ifSessionStateNotNull(key) && StringUtils.isNotBlank(String.valueOf(getSlotValue(key).orElse("")));
        return result(conditionalResult, conditionalText);
    }

    /**
     * Validates if a session attributes value is not blank.
     * @param key key of the session attribute.
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateNotBlank(final String key, final Consumer<AlexaSession> followUp) {
        if (ifSessionStateNotBlank(key)) {
            followUp.accept(this.request.getSession());
        }
        return this;
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
        return validate(ifSessionStateEquals(key, value), assertionText);
    }

    /**
     * Validates if a session attributes value is equal to the value given.
     * @param key key of the session attribute.
     * @param value expected value of the session attribute.
     * @return True, if the session attributes value ifEquals the value given
     */
    public boolean ifSessionStateEquals(final String key, final String value) {
        final String conditionalText = String.format("Session state with key '%1$s' is equal to '%2$s'.", key, value);
        final boolean conditionalResult = value != null ? ifSessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).equals(value) :
                (envelope.getSessionAttributes().getOrDefault(key, null) == null);
        return result(conditionalResult, conditionalText);
    }

    /**
     * Validates if a session attributes value is equal to the value given.
     * @param key key of the session attribute.
     * @param value expected value of the session attribute.
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateEquals(final String key, final String value, final Consumer<AlexaSession> followUp) {
        if (ifSessionStateEquals(key, value)) {
            followUp.accept(request.getSession());
        }
        return this;
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
        return validate(ifSessionStateContains(key, subString), assertionText);
    }

    /**
     * Validates if a session attributes value contains the string given.
     * @param key key of the session attribute.
     * @param subString expected string portion in session attribute value.
     * @return True, if the session attribute value contains the given string.
     */
    public boolean ifSessionStateContains(final String key, final String subString) {
        final String conditionalText = String.format("Session state with key '%1$s' contains '%2$s'.", key, subString);
        final boolean conditionalResult = ifSessionStateNotNull(key) && StringUtils.contains(String.valueOf(getSlotValue(key).orElse("")), subString);
        return result(conditionalResult, conditionalText);
    }

    /**
     * Validates if a session attributes value contains the string given.
     * @param key key of the session attribute.
     * @param subString expected string portion in session attribute value.
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateContains(final String key, final String subString, Consumer<AlexaSession> followUp) {
        if (ifSessionStateContains(key, subString)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * Validates if value of a session attribute ifMatch the pattern given. It throws an IllegalArgumentException
     * in case the session attributes value does not match with the pattern given.
     * @param key key of the session attribute.
     * @param regex regular expression
     * @return this response
     */
    public AlexaResponse assertSessionStateMatches(final String key, final String regex){
        final String assertionText = String.format("Session state with key '%1$s' ifMatch pattern '%2$s'.", key, regex);
        return validate(ifSessionStateMatches(key, regex), assertionText);
    }

    /**
     * Validates if value of a session attribute ifMatch the pattern given.
     * @param key key of the session attribute.
     * @param regex regular expression
     * @return True, if the value ifMatch the pattern given.
     */
    public boolean ifSessionStateMatches(final String key, final String regex) {
        final String conditionalText = String.format("Session state with key '%1$s' ifMatch pattern '%2$s'.", key, regex);
        final boolean conditionalResult = ifSessionStateNotNull(key) && String.valueOf(getSlotValue(key).orElse("")).matches(regex);
        return result(conditionalResult, conditionalText);
    }

    /**
     * Validates if value of a session attribute ifMatch the pattern given.
     * @param key key of the session attribute.
     * @param regex regular expression
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifSessionStateMatches(final String key, final String regex, Consumer<AlexaSession> followUp) {
        if (ifSessionStateMatches(key, regex)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    /**
     * exits the fluent interface and returns the actor in order to continue with
     * next skill request.
     * @return actor
     */
    public AlexaSession done() {
        return request.getSession();
    }

    private AlexaResponse validate(final boolean assertionResult, final String assertionText) {
        Validate.isTrue(assertionResult, "[FAILED] Assertion '%1$s' is FALSE.", assertionText);
        return this;
    }

    private boolean result(final boolean conditionalResult, final String conditionalText) {
        final String result = conditionalResult ? "TRUE" : "FALSE";
        log.info(String.format("->[%1$s] %2$s", result, conditionalText));
        return conditionalResult;
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
