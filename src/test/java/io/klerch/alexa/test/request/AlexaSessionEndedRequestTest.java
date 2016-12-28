package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.SessionEndedRequest;
import io.klerch.alexa.test.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

public class AlexaSessionEndedRequestTest {
    @Test
    public void expectsResponse() throws Exception {
        final AlexaSessionEndedRequest request = new AlexaSessionEndedRequest(AssetFactory.givenActor());
        Assert.assertFalse(request.expectsResponse());
    }

    @Test
    public void getSpeechletRequest() throws Exception {
        final AlexaSessionEndedRequest request = new AlexaSessionEndedRequest(AssetFactory.givenActor());
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof SessionEndedRequest);
    }
}