package io.klerch.alexa.tester.actor;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.tester.client.AlexaClient;
import io.klerch.alexa.tester.request.*;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlexaSessionActor extends AlexaActor {
    final Session session;
    boolean sessionClosed;

    public AlexaSessionActor(final AlexaClient client) {
        super(client);
        this.sessionClosed = false;
        this.session = Session.builder()
                .withApplication(client.getApplication())
                .withUser(client.getUser())
                .withIsNew(false)
                .withSessionId(UUID.randomUUID().toString())
                .build();
        client.fire(new AlexaSessionStartedRequest(this));
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
        }
    }

    @Override
    public SpeechletRequestEnvelope envelope(final AlexaRequest request) {
        // ensure session is ready for another request
        Validate.isTrue(!sessionClosed, "Session already closed and not ready for another request.");
        return SpeechletRequestEnvelope.builder()
                .withRequest(request.getSpeechletRequest())
                .withSession(request instanceof AlexaSessionStartedRequest ? getSessionWithIsNew() : session)
                .withVersion(client.VERSION)
                .build();
    }

    @Override
    public void exploitResponse(final AlexaResponse response) {
        if (!response.isEmpty()) {
            // remember session closed
            sessionClosed = response.getShouldEndSession();
            // apply session attributes for next request
            applySessionAttributes(response.getSessionAttributes());
        }
    }

    public AlexaResponse intent(final String intentName) {
        return client.fire(new AlexaIntentRequest(this, intentName)).orElseThrow(() ->
                new RuntimeException("Intent request did not receive a response.")
        );
    }

    public AlexaResponse intent(final String intentName, final String slotName, final Object slotValue) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlot(slotName, slotValue)).orElseThrow(() ->
                new RuntimeException("Intent request did not receive a response.")
        );
    }

    public AlexaResponse intent(final String intentName, final Map<String, Slot> slots) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlots(slots)).orElseThrow(() ->
                new RuntimeException("Intent request did not receive a response.")
        );
    }

    public AlexaResponse intent(final String intentName, final List<Slot> slots) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlots(slots)).orElseThrow(() ->
                new RuntimeException("Intent request did not receive a response.")
        );
    }

    private AlexaResponse intent(final AlexaIntentRequest request) {
        return client.fire(request).orElseThrow(() ->
                new RuntimeException("Intent Request did not receive a response.")
        );
    }

    public AlexaResponse launch() {
        return client.fire(new AlexaLaunchRequest(this)).orElseThrow(() ->
                new RuntimeException("Launch request did not receive a response.")
        );
    }

    public AlexaResponse repeat() {
        return intent( "AMAZON.RepeatIntent");
    }

    public AlexaResponse yes() {
        return intent("AMAZON.YesIntent");
    }

    public AlexaResponse no() {
        return intent("AMAZON.NoIntent");
    }

    public AlexaResponse cancel() {
        return intent("AMAZON.CancelIntent");
    }

    public AlexaResponse stop() {
        return intent("AMAZON.StopIntent");
    }

    public AlexaResponse startOver() {
        return intent("AMAZON.StartOverIntent");
    }

    public AlexaResponse next() {
        return intent("AMAZON.NextIntent");
    }

    public AlexaResponse previous() {
        return intent("AMAZON.PreviousIntent");
    }

    public AlexaResponse pause() {
        return intent("AMAZON.PauseIntent");
    }

    public AlexaResponse resume() {
        return intent("AMAZON.ResumeIntent");
    }

    public AlexaResponse shuffleOn() {
        return intent("AMAZON.ShuffleOnIntent");
    }

    public AlexaResponse shuffleOff() {
        return intent("AMAZON.ShuffleOffIntent");
    }

    public AlexaResponse loopOn() {
        return intent("AMAZON.LoopOnIntent");
    }

    public AlexaResponse loopOff() { return intent("AMAZON.LoopOffIntent"); }

    public AlexaResponse help() { return intent( "AMAZON.HelpIntent"); }

    public AlexaSessionActor delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void endSession() {
        endSession(SessionEndedRequest.Reason.USER_INITIATED);
    }

    public void endSession(final SessionEndedRequest.Reason reason) {
        client.fire(new AlexaSessionEndedRequest(this, reason));
    }
}
