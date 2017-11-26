package io.klerch.alexa.test.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import io.klerch.alexa.test.client.AlexaSession;
import io.klerch.alexa.test.request.AlexaRequest;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
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
     * @param jsonPathExpression a json-path expression
     * @return this response
     */
    public AlexaResponse assertThat(final String jsonPathExpression) {
        return validate(is(jsonPathExpression), "JSON path expression " + jsonPathExpression);
    }

    public AlexaResponse assertTrue(final String jsonPathExpression) {
        return assertThat(jsonPathExpression + " == true");
    }

    public AlexaResponse assertFalse(final String jsonPathExpression) {
        return assertThat(jsonPathExpression + " == false");
    }

    public AlexaResponse assertExists(final String jsonPathExpression) {
        return assertThat(jsonPathExpression);
    }

    public AlexaResponse assertNotExists(final String jsonPathExpression) {
        return assertExists("!(" + jsonPathExpression + ")");
    }

    public AlexaResponse assertEquals(final String jsonPathExpression, final Object o) {
        final String s = o instanceof String ? "'" + o + "'" : o.toString();
        return assertThat(jsonPathExpression + " == " + s);
    }

    public AlexaResponse assertContains(final String jsonPathExpression, final String s) {
        return assertThat(jsonPathExpression + " =~ /.*" + s + ".*/i");
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
     * @param jsonPathExpression a json-path expression
     * @return True, if expression returns an element
     */
    public boolean is(String jsonPathExpression) {
        final String conditionalText = String.format("%1$s is TRUE.", jsonPathExpression);
        // turn simplified into valid JSONPath expression
        if (!jsonPathExpression.startsWith("?(@.")) {
            jsonPathExpression = "?(@." + jsonPathExpression + ")";
        }
        // wrap validation expression
        final String jsonPath = "$.response[" + jsonPathExpression + "]";
        // wrap response payload
        final Object o = JsonPath.using(config).parse("{ \"response\" : [ " + responsePayload + " ]}").read(jsonPath);
        // validate expression
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
     * Validates a custom predicate executed against the response envelope.
     * @param jsonPathExpression the response envelope of the request
     * @param followUp code-block executes when condition is true
     * @return this response
     */
    public AlexaResponse ifIs(final String jsonPathExpression, final Consumer<AlexaSession> followUp) {
        if (is(jsonPathExpression)) {
            followUp.accept(request.getSession());
        }
        return this;
    }

    public boolean isTrue(final String jsonPathExpression) {
        return is(jsonPathExpression + " == true");
    }

    public boolean isFalse(final String jsonPathExpression) {
        return is(jsonPathExpression + " == false");
    }

    public boolean exists(final String jsonPathExpression) {
        return is(jsonPathExpression);
    }

    public boolean notExists(final String jsonPathExpression) {
        return is("!(" + jsonPathExpression + ")");
    }

    public boolean equals(final String jsonPathExpression, final Object o) {
        final String s = o instanceof String ? "'" + o + "'" : o.toString();
        return is(jsonPathExpression + " == " + s);
    }

    public boolean contains(final String jsonPathExpression, final String s) {
        return is(jsonPathExpression + " =~ /.*" + s + ".*/i");
    }

    public AlexaResponse ifTrue(final String jsonPathExpression, final Consumer<AlexaSession> followUp) {
        return ifIs(jsonPathExpression + " == true", followUp);
    }

    public AlexaResponse ifFalse(final String jsonPathExpression, final Consumer<AlexaSession> followUp) {
        return ifIs(jsonPathExpression + " == false", followUp);
    }

    public AlexaResponse ifExists(final String jsonPathExpression, final Consumer<AlexaSession> followUp) {
        return ifIs(jsonPathExpression, followUp);
    }

    public AlexaResponse ifNotExists(final String jsonPathExpression, final Consumer<AlexaSession> followUp) {
        return ifIs("!(" + jsonPathExpression + ")", followUp);
    }

    public AlexaResponse ifEquals(final String jsonPathExpression, final Object o, final Consumer<AlexaSession> followUp) {
        final String s = o instanceof String ? "'" + o + "'" : o.toString();
        return ifIs(jsonPathExpression + " == " + s, followUp);
    }

    public AlexaResponse ifContains(final String jsonPathExpression, final String s, final Consumer<AlexaSession> followUp) {
        return ifIs(jsonPathExpression + " =~ /.*" + s + ".*/i", followUp);
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
}
