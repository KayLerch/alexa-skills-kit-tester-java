package io.klerch.alexa.tester.request;

import com.amazon.speech.speechlet.SessionStartedRequest;
import io.klerch.alexa.tester.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

public class AlexaSessionStartedRequestTest {
    @Test
    public void expectsResponse() throws Exception {
        final AlexaSessionStartedRequest request = new AlexaSessionStartedRequest(AssetFactory.givenActor());
        Assert.assertFalse(request.expectsResponse());
    }

    @Test
    public void getSpeechletRequest() throws Exception {
        final AlexaSessionStartedRequest request = new AlexaSessionStartedRequest(AssetFactory.givenActor());
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof SessionStartedRequest);
    }
}