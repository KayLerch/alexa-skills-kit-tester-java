package io.klerch.alexa.test.client;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;

public class AlexaLambdaClientTest extends AlexaClientTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    AWSLambda givenLambdaMock() {
        return Mockito.mock(AWSLambdaClient.class, (Answer) invocationOnMock -> {
            if (invocationOnMock.getMethod().getName().equals("invoke")) {
                final InvokeRequest request = invocationOnMock.getArgumentAt(0, InvokeRequest.class);
                final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(request.getPayload());
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // delegate request to stream handler
                givenHandler().handleRequest(inputStream, outputStream, givenContext());
                // put result into payload
                return request.getInvocationType().equals(InvocationType.RequestResponse.name()) ?
                        new InvokeResult().withPayload(ByteBuffer.wrap(outputStream.toByteArray())) : new InvokeResult();
            }
            return invocationOnMock.callRealMethod();
        });
    }

    @Override
    public AlexaClient givenClient() throws Exception {
        return AlexaLambdaClient
                .create(AlexaClient.generateApplicationId(), "lambda-function-name")
                .withLambdaClient(givenLambdaMock())
                .withDebugFlagSessionAttribute("myDebugFlag")
                .build();
    }

    @Test
    public void createInvalid() throws Exception {
        exception.expect(NullPointerException.class);
        AlexaLambdaClient.create(AlexaClient.generateApplicationId(), null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaLambdaClient.create("", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaLambdaClient.create(null, "lambda-function-name").build();
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final AlexaLambdaClient test1 = AlexaLambdaClient.create("appId", "lambda-function-name").build();
        Assert.assertEquals(test1.getLocale(), Locale.US);
        Assert.assertEquals(test1.getLambdaFunctionName(), "lambda-function-name");
        Assert.assertNotNull(test1.getLambdaClient());
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final Date timestamp = DateUtils.addDays(new Date(), 5);
        final AWSLambda lambdaClient = new AWSLambdaClient();
        final AlexaLambdaClient test1 = AlexaLambdaClient
                .create("appId", "lambda-function-name")
                .withLambdaClient(lambdaClient)
                .withLocale(Locale.GERMANY)
                .withUserId("uid")
                .withTimestamp(timestamp)
                .withAccessToken("accessToken")
                .withDebugFlagSessionAttribute("debug123flag")
                .build();

        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getLambdaClient(), lambdaClient);
        Assert.assertEquals(test1.getLambdaFunctionName(), "lambda-function-name");
        Assert.assertEquals(test1.getDebugFlagSessionAttributeName(), Optional.of("debug123flag"));
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
        Assert.assertEquals(test1.getUser().getUserId(), "uid");
        Assert.assertEquals(test1.getUser().getAccessToken(), "accessToken");
    }
}