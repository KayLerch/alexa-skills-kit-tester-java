package io.klerch.alexa.tester.client;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.tester.request.*;
import io.klerch.alexa.tester.response.AlexaResponse;

import java.util.List;
import java.util.Map;

public class AlexaTestActor {
    private final AlexaTest client;

    public AlexaTestActor(final AlexaTest client) {
        this.client = client;
        client.fire(new AlexaSessionStartedRequest(this));
    }

    public AlexaTest getClient() {
        return this.client;
    }

    public AlexaResponse intent(final String intentName) {
        return client.fire(new AlexaIntentRequest(this, intentName));
    }

    public AlexaResponse intent(final String intentName, final String slotName, final Object slotValue) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlot(slotName, slotValue));
    }

    public AlexaResponse intent(final String intentName, final Map<String, Slot> slots) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlots(slots));
    }

    public AlexaResponse intent(final String intentName, final List<Slot> slots) {
        return client.fire(new AlexaIntentRequest(this, intentName).withSlots(slots));
    }

    public AlexaResponse intent(final AlexaIntentRequest request) {
        return client.fire(request);
    }

    public AlexaResponse launch() {
        return client.fire(new AlexaLaunchRequest(this));
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

    public void end() {
        end(SessionEndedRequest.Reason.USER_INITIATED);
    }

    public void end(final SessionEndedRequest.Reason reason) {
        client.fire(new AlexaSessionEndedRequest(this, reason));
    }
}
