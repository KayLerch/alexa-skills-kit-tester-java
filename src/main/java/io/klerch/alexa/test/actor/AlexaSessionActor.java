package io.klerch.alexa.test.actor;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.test.client.AlexaClient;
import io.klerch.alexa.test.request.*;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            sessionClosed = response.sessionEnded();
            // apply session attributes for next request
            applySessionAttributes(response.getSessionAttributes());
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
}
