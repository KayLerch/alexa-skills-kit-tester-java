package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.request.*;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.joox.Match;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

import static org.joox.JOOX.$;

/**
 * The session actor manages a conversation within a single Alexa session by
 * persisting session state.
 */
public class AlexaSessionActor extends AlexaActor {
    private final static Logger log = Logger.getLogger(AlexaClient.class);
    final Session session;
    boolean sessionClosed;

    public static String generateSessionId() {
        return String.format("SessionId.%s", UUID.randomUUID());
    }

    public AlexaSessionActor(final AlexaClient client) {
        super(client);
        this.sessionClosed = false;
        this.session = Session.builder()
                .withApplication(client.getApplication())
                .withUser(client.getUser())
                .withIsNew(false)
                .withSessionId(generateSessionId())
                .withAttributes(new LinkedHashMap<>())
                .build();

        // if debug flag is set add it to the session attributes
        this.getClient().getDebugFlagSessionAttributeName().ifPresent(name -> {
            this.session.getAttributes().putIfAbsent(name, true);
        });

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
        // ensure session is ready for another request
        Validate.isTrue(!sessionClosed, "Session already closed and not ready for another request.");
        return SpeechletRequestEnvelope.builder()
                .withRequest(request.getSpeechletRequest())
                .withSession(request instanceof AlexaSessionStartedRequest ? getSessionWithIsNew() : session)
                .withVersion(AlexaClient.VERSION)
                .build();
    }

    @Override
    public void exploitResponse(final AlexaResponse response) {
        if (!response.isEmpty()) {
            // remember session closed
            sessionClosed = response.getResponseEnvelope().getResponse().getShouldEndSession();
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
    public AlexaResponse intent(final String intentName, final Map<String, Slot> slots) {
        return intent(new AlexaIntentRequest(this, intentName).withSlots(slots));
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
        log.info(String.format("\n[START] intent request '%s' ...", request.getIntentName()));
        final AlexaResponse response = client.fire(request).orElseThrow(() ->
                new RuntimeException("[ERROR] intent request did not receive a response.")
        );
        log.info(String.format("[DONE] intent request '%1$s' in %2$s ms.", request.getIntentName(), getClient().getLastExecutionMillis()));
        return response;
    }

    /**
     * Fires a launch request
     * @return skill's response
     */
    public AlexaResponse launch() {
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
    public AlexaSessionActor delay(long millis) {
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

    void executeSession(final Element session) {
        $(session).children().forEach(processActionInSession);
        this.endSession();
    }

    private Consumer<Element> processActionInSession = request -> {
        final String requestName = request.getLocalName();
        // in case request is a delay, must provide a value as attribute
        Validate.isTrue(request.hasAttribute("value") || !"delay".equals(requestName), "[ERROR] Delay must have a value provided as an attribute of delay-tag in your script-file. The value should be numeric and indicates the milliseconds to wait for the next request.");
        // in case request is a custom intent, must provide a name as attribute
        Validate.isTrue(request.hasAttribute("name") || !"intent".equals(requestName), "[ERROR] Intent must have a name provided as an attribute of intent-tag in your script-file.");
        // request must match method in actor
        Validate.notNull(Arrays.stream(AlexaSessionActor.class.getMethods()).anyMatch(method -> method.getName().equals(requestName)), "[ERROR] Unknown request-type '%s' found in your script-file.", requestName);

        if ("delay".equals(requestName)) {
            delay(Long.parseLong(request.getAttribute("value")));
        } else {
            try {
                // request the skill with intent
                final AlexaResponse response = "intent".equals(requestName) ?
                        intent(request.getAttribute("name"), extractSlots($(request))) :
                        (AlexaResponse)AlexaSessionActor.class.getMethod(requestName).invoke(this);
                // validate response
                validateResponse(response, $(request));
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                final String msg = String.format("[ERROR] The request '%1$s' in your script-file is not supported. %2$s", request.getTagName(), e.getMessage());
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    };


    private void validateResponse(final AlexaResponse response, final Match mResponse) {
        if (mResponse.isEmpty()) {
            log.info("[INFO] No assertions defined in your script-file to validate skill's response.");
            return;
        }

        mResponse.children().forEach(assertion -> {
            final String assertionMethod = assertion.getTagName();
            final String assetName = assertion.getAttribute("asset");
            final String assertionName = assertion.getAttribute("assertion");
            final String key = assertion.getAttribute("key");
            final String value = assertion.getAttribute("value");

            // skip request node which is at the same level as all the assertion-tags
            if ("request".equals(assertionMethod)) return;

            //Validate.matchesPattern(assertionMethod, "assert.*", "[ERROR] Invalid assertion method '%s' in your script-file.", assertionMethod);
            Validate.isTrue(Arrays.stream(response.getClass().getMethods()).anyMatch(m -> m.getName().equals(assertionMethod)), "[ERROR] Invalid assertion method '%s' in your script-file.", assertionMethod);

            Arrays.stream(response.getClass().getMethods())
                    .filter(m -> m.getName().equals(assertionMethod))
                    .findFirst()
                    .ifPresent(m -> {
                        final List<Object> parameters = new ArrayList<>();
                        if (StringUtils.containsIgnoreCase(m.getName(), "SessionState")) {
                            if (m.getParameterCount() > 0) {
                                parameters.add(key);
                            }
                            if (m.getParameterCount() > 1) {
                                parameters.add(value);
                            }
                        } else {
                            Arrays.stream(m.getParameterTypes()).forEach(pc -> {
                                // assign value to parameter based on its type
                                parameters.add(AlexaAsset.class.equals(pc) ?
                                        Enum.valueOf(AlexaAsset.class, assetName) :
                                        AlexaAssertion.class.equals(pc) ?
                                                Enum.valueOf(AlexaAssertion.class, assertionName) :
                                                long.class.equals(pc) ?
                                                        Long.parseLong(value) :
                                                        pc.cast(value));
                            });
                        }
                        try {
                            final Object result = m.invoke(response, parameters.toArray());
                            // result only for conditional methods and those are always booleans
                            if (result != null && Boolean.parseBoolean(result.toString())) {
                                // in that case a conditional was true. The body should contain actions to perform (recursion starts)
                                $(assertion).children().forEach(processActionInSession);
                            }
                        } catch (final InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
        });
    }

    private List<Slot> extractSlots(final Match mRequest) {
        final List<Slot> slots = new ArrayList<>();
        mRequest.find("request").find("slots").find("slot").forEach(mSlot -> {
            if (mSlot.hasAttribute("key") && mSlot.hasAttribute("value")) {
                slots.add(Slot.builder()
                        .withName(mSlot.getAttribute("key"))
                        .withValue(mSlot.getAttribute("value")).build());
            }
        });
        return slots;
    }
}
