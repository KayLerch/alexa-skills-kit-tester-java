package io.klerch.alexa.test.request;

import com.amazon.speech.speechlet.LaunchRequest;
import io.klerch.alexa.test.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

public class AlexaLaunchRequestTest {
    @Test
    public void expectsResponse() throws Exception {
        final AlexaLaunchRequest request = new AlexaLaunchRequest(AssetFactory.givenActor());
        Assert.assertTrue(request.expectsResponse());
    }

    @Test
    public void getSpeechletRequest() throws Exception {
        final AlexaLaunchRequest request = new AlexaLaunchRequest(AssetFactory.givenActor());
        Assert.assertNotNull(request.getSpeechletRequest());
        Assert.assertTrue(request.getSpeechletRequest() instanceof LaunchRequest);
    }
}