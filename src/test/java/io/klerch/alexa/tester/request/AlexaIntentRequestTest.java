package io.klerch.alexa.tester.request;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import io.klerch.alexa.tester.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class AlexaIntentRequestTest {

    @Test
    public void expectsResponse() throws Exception {
        final AlexaIntentRequest request = new AlexaIntentRequest(AssetFactory.givenActor(), "intent");
        Assert.assertTrue(request.expectsResponse());
    }

    @Test
    public void getSpeechletRequest() throws Exception {
        final AlexaIntentRequest request = new AlexaIntentRequest(AssetFactory.givenActor(), "intent");
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof IntentRequest);
        final IntentRequest intentRequest = (IntentRequest)request.getSpeechletRequest();

        Assert.assertEquals("intent", intentRequest.getIntent().getName());
    }

    @Test
    public void getSpeechletRequestWithSlots() throws Exception {
        final AlexaIntentRequest request = new AlexaIntentRequest(AssetFactory.givenActor(), "intent")
                .withSlot("slotName", true);
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof IntentRequest);
        final IntentRequest intentRequest = (IntentRequest)request.getSpeechletRequest();

        Assert.assertEquals("intent", intentRequest.getIntent().getName());
        Assert.assertNotNull(intentRequest.getIntent().getSlot("slotName"));
        Assert.assertEquals("true", intentRequest.getIntent().getSlot("slotName").getValue());
    }

    @Test
    public void getSpeechletRequestWithSlots2() throws Exception {
        final Slot slot1 = Slot.builder().withName("slotName1").withValue("123").build();
        final Slot slot2 = Slot.builder().withName("slotName2").withValue("321").build();

        final AlexaIntentRequest request = new AlexaIntentRequest(AssetFactory.givenActor(), "intent")
                .withSlots(Arrays.asList(slot1, slot2));
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof IntentRequest);
        final IntentRequest intentRequest = (IntentRequest)request.getSpeechletRequest();

        Assert.assertEquals("intent", intentRequest.getIntent().getName());
        Assert.assertNotNull(intentRequest.getIntent().getSlot("slotName1"));
        Assert.assertNotNull(intentRequest.getIntent().getSlot("slotName2"));
        Assert.assertEquals("123", intentRequest.getIntent().getSlot("slotName1").getValue());
        Assert.assertEquals("321", intentRequest.getIntent().getSlot("slotName2").getValue());
    }
}