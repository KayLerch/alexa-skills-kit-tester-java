package io.klerch.alexa.test.client.endpoint;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlexaLambdaEndpointTest {
    @Test
    public void create() throws Exception {
        final AWSLambda lambda = new AWSLambdaClient();
        final AlexaLambdaEndpoint ep = AlexaLambdaEndpoint.create("lambdaFunctionName").withLambdaClient(lambda).build();
        Assert.assertEquals(ep.getLambdaFunctionName(), "lambdaFunctionName");
        Assert.assertEquals(ep.getLambdaClient(), lambda);
    }
}