package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import io.klerch.alexa.test.client.endpoint.AlexaSimulationApiEndpoint;
import io.klerch.alexa.test.request.*;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * The session actor manages a conversation within a single Alexa session by
 * persisting session state.
 */
public class AlexaSession extends AlexaActor {
    private final static Logger log = Logger.getLogger(AlexaClient.class);
    final Session session;
    boolean sessionClosed;

    public static String generateSessionId() {
        return String.format("SessionId.%s", UUID.randomUUID());
    }

    public AlexaSession(final AlexaClient client, final Session session) {
        super(client);
        this.sessionClosed = false;
        this.session = session;
        // if debug flag is set add it to the session attributes
        this.getClient().getDebugFlagSessionAttributeName().ifPresent(name -> {
            this.session.getAttributes().putIfAbsent(name, true);
        });
    }

    public AlexaSession(final AlexaClient client) {
        this(client, Session.builder()
                .withApplication(client.getApplication())
                .withUser(client.getUser())
                .withIsNew(false)
                .withSessionId(generateSessionId())
                .withAttributes(new LinkedHashMap<>())
                .build());

        log.info(String.format("\n[START] session start request with sessionId '%s' ...", this.session.getSessionId()));
        client.fire(new AlexaSessionStartedRequest(this));
        log.info("[DONE] session start request.");
    }

    private Session getSessionWithIsNew() {
        return Session.builder()
                .withApplication(session.getApplication())
                .withUser(session.getUser())
                .withIsNew(true)
                .withSessionId(session.getSessionId())
                .withAttributes(session.getAttributes())
                .build();
    }

    private void applySessionAttributes(final Map<String, Object> attributes) {
        // reset attributes first
        this.session.getAttributes().clear();
        if (attributes != null) {
            // apply attributes one by one
            attributes.forEach(this.session::setAttribute);
            // if debug flag is set add it to the session attributes
            this.getClient().getDebugFlagSessionAttributeName().ifPresent(name -> {
                attributes.putIfAbsent(name, true);
            });
        }
    }

    @Override
    public SpeechletRequestEnvelope envelope(final AlexaRequest request) {
        final SystemState systemState = SystemState.builder()
                .withUser(session.getUser())
                .withDevice(client.device)
                .withApiEndpoint(client.apiEndpoint)
                .withApplication(session.getApplication()).build();

        final Context context = Context.builder().addState(systemState).build();

        // ensure session is ready for another request (make an exception for session ended and launch requests)
        Validate.isTrue(!sessionClosed || AlexaSessionEndedRequest.class.isInstance(request) || AlexaLaunchRequest.class.isInstance(request), "Session already closed and not ready for another request.");
        return SpeechletRequestEnvelope.builder()
                .withRequest(request.getSpeechletRequest())
                .withSession(request instanceof AlexaSessionStartedRequest ? getSessionWithIsNew() : session)
                .withVersion(AlexaClient.VERSION)
                .withContext(context)
                .build();
    }

    @Override
    public void exploitResponse(final AlexaResponse response) {
        if (!response.isEmpty() && response.getResponseEnvelope() != null && response.getResponseEnvelope().getResponse() != null) {
            // remember session closed
            sessionClosed = response.getResponseEnvelope().getResponse().getNullableShouldEndSession();
            // apply session attributes for next request
            applySessionAttributes(response.getResponseEnvelope().getSessionAttributes());
        }
    }

    /**
     * Fires an intent without any slots.
     * @param intentName name of the intent
     * @return skill's response
     */
    public AlexaResponse intent(final String intentName) {
        return intent(new AlexaIntentRequest(this, intentName));
    }

    /**
     * Fires an intent with a single slot
     * @param intentName name of the intent
     * @param slotName name of the slot
     * @param slotValue value of the slot
     * @return skill's response
     */
    public AlexaResponse intent(final String intentName, final String slotName, final Object slotValue) {
        return intent(new AlexaIntentRequest(this, intentName).withSlot(slotName, slotValue));
    }

    /**
     * Fires an intent with zero to many slots
     * @param intentName name of the intent
     * @param slots collection of slots
     * @return skill's response
     */
    public AlexaResponse intent(final String intentName, final Map<String, Object> slots) {
        final Map<String, Slot> slots2 = new HashMap<>();
        slots.forEach((k,v) -> {
            slots2.putIfAbsent(k, v instanceof Slot ? (Slot)v : Slot.builder().withName(k).withValue(v.toString()).build());
        });

        return intent(new AlexaIntentRequest(this, intentName).withSlots(slots2));
    }

    /**
     * Fires an intent with zero to many slots
     * @param intentName name of the intent
     * @param slots collection of slots
     * @return skill's response
     */
    public AlexaResponse intent(final String intentName, final List<Slot> slots) {
        return intent(new AlexaIntentRequest(this, intentName).withSlots(slots));
    }

    private AlexaResponse intent(final AlexaIntentRequest request) {
        log.info(String.format("\n[START] intent request '%1$s' %2$s ...", request.getIntentName(), request.getSlotSummary()));
        final AlexaResponse response = client.fire(request).orElseThrow(() ->
                new RuntimeException("[ERROR] intent request did not receive a response.")
        );
        log.info(String.format("[DONE] intent request '%1$s' in %2$s ms.", request.getIntentName(), getClient().getLastExecutionMillis()));
        return response;
    }

    public AlexaResponse say(final String utterance) {
        Validate.isTrue(this.getClient().endpoint instanceof AlexaSimulationApiEndpoint, "Utterance requests are only supported by SimulationApi-Endpoints.");

        log.info(String.format("\n[START] utterance request '%s' ...", utterance));
        final AlexaResponse response = client.fire(new AlexaUtteranceRequest(this), utterance).orElseThrow(() ->
                new RuntimeException("[ERROR] utterance request did not receive a response.")
        );
        log.info(String.format("[DONE] utterance request '%1$s' in %2$s ms.", utterance, getClient().getLastExecutionMillis()));
        return response;
    }

    /**
     * Fires a launch request
     * @return skill's response
     */
    public AlexaResponse launch() {
        // reset attributes first
        this.session.getAttributes().clear();

        log.info("\n[START] launch request ...");
        final AlexaResponse response = client.fire(new AlexaLaunchRequest(this)).orElseThrow(() ->
                new RuntimeException("[ERROR] launch request did not receive a response.")
        );
        log.info(String.format("[DONE] launch request in %1$s ms.", getClient().getLastExecutionMillis()));
        return response;
    }

    /**
     * Fires the builtin repeat-intent.
     * @return skill's response
     */
    public AlexaResponse repeat() {
        return intent( "AMAZON.RepeatIntent");
    }

    /**
     * Fires the builtin yes-intent.
     * @return skill's response
     */
    public AlexaResponse yes() {
        return intent("AMAZON.YesIntent");
    }

    /**
     * Fires the builtin no-intent.
     * @return skill's response
     */
    public AlexaResponse no() {
        return intent("AMAZON.NoIntent");
    }

    /**
     * Fires the builtin cancel-intent.
     * @return skill's response
     */
    public AlexaResponse cancel() {
        return intent("AMAZON.CancelIntent");
    }

    /**
     * Fires the builtin stop-intent.
     * @return skill's response
     */
    public AlexaResponse stop() {
        return intent("AMAZON.StopIntent");
    }

    /**
     * Fires the builtin start-over-intent.
     * @return skill's response
     */
    public AlexaResponse startOver() {
        return intent("AMAZON.StartOverIntent");
    }

    /**
     * Fires the builtin next-intent.
     * @return skill's response
     */
    public AlexaResponse next() {
        return intent("AMAZON.NextIntent");
    }

    /**
     * Fires the builtin previous-intent.
     * @return skill's response
     */
    public AlexaResponse previous() {
        return intent("AMAZON.PreviousIntent");
    }

    /**
     * Fires the builtin pause-intent.
     * @return skill's response
     */
    public AlexaResponse pause() {
        return intent("AMAZON.PauseIntent");
    }

    /**
     * Fires the builtin resume-intent.
     * @return skill's response
     */
    public AlexaResponse resume() {
        return intent("AMAZON.ResumeIntent");
    }

    /**
     * Fires the builtin shuffle-on-intent.
     * @return skill's response
     */
    public AlexaResponse shuffleOn() {
        return intent("AMAZON.ShuffleOnIntent");
    }

    /**
     * Fires the builtin shuffle-off-intent.
     * @return skill's response
     */
    public AlexaResponse shuffleOff() {
        return intent("AMAZON.ShuffleOffIntent");
    }

    /**
     * Fires the builtin loop-on-intent.
     * @return skill's response
     */
    public AlexaResponse loopOn() {
        return intent("AMAZON.LoopOnIntent");
    }

    /**
     * Fires the builtin loop-off-intent.
     * @return skill's response
     */
    public AlexaResponse loopOff() { return intent("AMAZON.LoopOffIntent"); }

    /**
     * Fires the builtin help-intent.
     * @return skill's response
     */
    public AlexaResponse help() { return intent( "AMAZON.HelpIntent"); }

    /**
     * Sleeps for some time to delay the next request. This might be useful if your
     * skill has time-dependant behaviors.
     * @param millis milliseconds to sleep
     * @return skill's response
     */
    public AlexaSession delay(long millis) {
        log.info(String.format("\n[START] wait for %s ms.", millis));
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            final String msg = String.format("Error while waiting for %1$s ms caused by %2$s", millis, e.getMessage());
            log.error(String.format("[ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        log.info(String.format("[DONE] wait for %s ms.", millis));
        return this;
    }

    public void endSession() {
        endSession(SessionEndedRequest.Reason.USER_INITIATED);
    }

    public void endSession(final SessionEndedRequest.Reason reason) {
        log.info(String.format("\n[START] request session end with reason '%s'.", reason.name()));
        client.fire(new AlexaSessionEndedRequest(this, reason));
        log.info(String.format("[DONE] request session end with reason '%s'.", reason.name()));
    }

    void executeSession(final Object yLaunch) {
        if (yLaunch instanceof Optional<?>) {
            ((Optional<?>)yLaunch).ifPresent(launch -> {
                executeAction((ArrayList)launch);
            });
        } else if (yLaunch instanceof ArrayList) {
            executeAction((ArrayList)yLaunch);
        }
        else {
            log.warn("Launch node is of unexpected type.");
        }
    }

    @SuppressWarnings("Unchecked")
    void executeAction(final ArrayList assets) {
        final List<String> assertions = new ArrayList<>();
        final Map<String, Object> params = new HashMap<>();
        final Map<String, ArrayList> conditions = new HashMap<>();
        final List<ArrayList> followUps = new ArrayList<>();
        final AtomicReference<String> intentName = new AtomicReference<>();
        final AtomicReference<String> utterance = new AtomicReference<>();

        assets.forEach(asset -> {
            // if asset is not a key-value treat it as an assertion
            if (asset instanceof String) assertions.add(asset.toString());
            // if asset is a map it could either be a parameter or a condition with reference
            else if (asset instanceof Map) {
                Map<Object, Object> kv = (Map)asset;
                if (kv.values().stream().allMatch(v -> v instanceof String)) {
                    kv.forEach((k, v) -> {
                        if (StringUtils.equalsIgnoreCase(k.toString(), "intent")) {
                            intentName.set(v.toString());
                        } else if (StringUtils.equalsIgnoreCase(k.toString(), "utterance")) {
                            utterance.set(v.toString());
                        } else {
                            final String value = v.toString();
                            // check if value assignment is actually a json expression
                            if (value.startsWith("$")) {
                                // try resolve json expression with last response
                                final String resolvedValue = Optional.ofNullable(client.getLastResponse())
                                        .map(lr -> lr.get(value).orElse(null))
                                        .filter(Objects::nonNull)
                                        .orElseThrow(() -> new RuntimeException(value + " could not be resolved for slot " + k));
                                params.putIfAbsent(k.toString(), resolvedValue);
                            } else {
                                params.putIfAbsent(k.toString(), value);
                            }
                        }
                    });
                } else if (kv.values().stream().allMatch(v -> v instanceof ArrayList)) {
                    kv.forEach((k, v) -> {
                        conditions.putIfAbsent(k.toString(), (ArrayList)v);
                    });
                }
            }
            else if (asset instanceof ArrayList) {
                followUps.add((ArrayList)asset);
            }
        });

        // fire request
        final AlexaResponse response =
                StringUtils.isNotBlank(intentName.get()) ? intent(intentName.get(), params) :
                        StringUtils.isNotBlank(utterance.get()) ? say(utterance.get()) : launch();

        // go through assertions
        assertions.forEach(response::assertThat);

        // go through all conditions
        conditions.forEach((condition, followUp) -> {
            // if condition met
            if (response.is(condition)) {
                executeAction(followUp);
            }
        });

        // follow up with standalone anchors
        followUps.forEach(this::executeAction);
    }
}
