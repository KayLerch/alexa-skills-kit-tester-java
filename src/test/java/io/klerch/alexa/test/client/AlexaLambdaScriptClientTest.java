package io.klerch.alexa.test.client;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class AlexaLambdaScriptClientTest extends AlexaLambdaClientTest {
    @Test
    public void startScript() throws Exception {
        givenClient().startScript();
    }

    @Override
    public AlexaLambdaScriptClient givenClient() throws Exception {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("script-max.xml");
        return AlexaLambdaScriptClient
                .create(stream)
                .withLambdaClient(givenLambdaMock())
                .build();
    }

    @Test
    public void createInvalid() throws Exception {
        exception.expect(NullPointerException.class);
        AlexaLambdaScriptClient.create(AlexaClient.generateApplicationId(), null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaLambdaScriptClient.create("", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaLambdaScriptClient.create(null, "lambda-function-name").build();

        exception.expect(IOException.class);
        AlexaLambdaScriptClient.create(getClass().getClassLoader().getResourceAsStream("doesnotexist.xml")).build();

        exception.expect(IOException.class);
        AlexaLambdaScriptClient.create(new URL("https", "doesnotexist", "doesnotexist.xml")).build();

        exception.expect(IOException.class);
        AlexaLambdaScriptClient.create(new URI("https", "doesnotexist", "doesnotexist.xml")).build();
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("script-min.xml");
        final AlexaLambdaScriptClient test1 = AlexaLambdaScriptClient.create(stream).build();
        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getLambdaFunctionName(), "myLambdaFunctionName");
        Assert.assertNotNull(test1.getLambdaClient());
        Assert.assertEquals(test1.getApplication().getApplicationId(), "myApplicationId");
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final AWSLambda lambdaClient = new AWSLambdaClient();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("script-max.xml");
        final AlexaLambdaScriptClient test1 = AlexaLambdaScriptClient.create(stream)
                .withLambdaClient(lambdaClient)
                .build();
        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getLambdaClient(), lambdaClient);
        Assert.assertEquals(test1.getLambdaFunctionName(), "myLambdaFunctionName");
        Assert.assertEquals(test1.getDebugFlagSessionAttributeName(), Optional.of("myDebugFlag"));
        Assert.assertEquals(test1.getApplication().getApplicationId(), "myApplicationId");
        Assert.assertEquals(test1.getUser().getUserId(), "myUserId");
        Assert.assertEquals(test1.getUser().getAccessToken(), "myAccessToken");
        Assert.assertTrue(DateUtils.isSameDay(test1.getCurrentTimestamp(), new GregorianCalendar(2010, 8, 30).getTime()));
    }
}