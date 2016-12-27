package io.klerch.alexa.tester.request;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.CoreSpeechletRequest;
import com.amazon.speech.speechlet.IntentRequest;
import io.klerch.alexa.tester.actor.AlexaSessionActor;

import java.util.*;

public class AlexaIntentRequest extends AlexaRequest {
    private final String intentName;
    private final Map<String, Slot> slots = new HashMap<>();

    public AlexaIntentRequest(final AlexaSessionActor actor, final String intentName) {
        super(actor);
        this.intentName = intentName;
    }

    public String getIntentName() {
        return this.intentName;
    }

    @Override
    public boolean expectsResponse() {
        return true;
    }

    public AlexaIntentRequest withSlot(final String slotName, final Object slotValue) {
        return withSlots(Collections.singletonList(
                Slot.builder()
                        .withName(slotName)
                        .withValue(String.valueOf(slotValue))
                        .build()));
    }

    public AlexaIntentRequest withSlots(final Map<String, Slot> slots) {
        this.slots.putAll(slots);
        return this;
    }

    public AlexaIntentRequest withSlots(final List<Slot> slots) {
        slots.forEach(slot -> this.slots.putIfAbsent(slot.getName(), slot));
        return this;
    }

    @Override
    public CoreSpeechletRequest getSpeechletRequest() {
        final Intent intent = Intent.builder()
                .withName(intentName)
                .withSlots(slots)
                .build();
        return IntentRequest.builder()
                .withLocale(actor.getClient().getLocale())
                .withRequestId(generateRequestId())
                .withIntent(intent)
                .withTimestamp(new Date())
                .build();
    }
}
