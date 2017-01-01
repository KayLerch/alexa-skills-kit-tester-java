package io.klerch.alexa.test.client.endpoint;

import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.client.endpoint.samples.AlexaRequestHandler;
import io.klerch.alexa.test.client.endpoint.samples.AlexaRequestStreamHandler;
import org.joox.Match;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.joox.JOOX.$;

public class AlexaEndpointFactoryTest {
    @Test
    public void createLambdaEndpoint() throws Exception {
        final AlexaEndpoint endpoint = AssetFactory.givenLambdaEndpoint();
        Assert.assertNotNull(endpoint);
        Assert.assertEquals(((AlexaLambdaEndpoint)endpoint).getLambdaFunctionName(), "lambdaFunctionName");
    }

    @Test
    public void createRequestStreamHandlerEndpoint() throws Exception {
        final AlexaEndpoint endpoint = AssetFactory.givenRequestStreamHandlerEndpoint();
        Assert.assertNotNull(endpoint);
        Assert.assertTrue(((AlexaRequestStreamHandlerEndpoint)endpoint).getRequestStreamHandler() instanceof AlexaRequestStreamHandler);
    }
}