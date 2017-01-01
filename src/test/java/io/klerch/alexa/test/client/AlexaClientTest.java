package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.*;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.client.endpoint.AlexaEndpoint;
import io.klerch.alexa.test.client.endpoint.AlexaLambdaEndpoint;
import io.klerch.alexa.test.client.endpoint.AlexaRequestStreamHandlerEndpoint;
import io.klerch.alexa.test.client.endpoint.samples.AlexaRequestStreamHandler;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;

public class AlexaClientTest {
    AWSLambda givenLambdaMock() {
        return Mockito.mock(AWSLambdaClient.class, (Answer) invocationOnMock -> {
            if (invocationOnMock.getMethod().getName().equals("invoke")) {
                final InvokeRequest request = invocationOnMock.getArgumentAt(0, InvokeRequest.class);
                final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(request.getPayload());
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // delegate request to stream handler
                givenHandler().handleRequest(inputStream, outputStream, AssetFactory.givenContext());
                // put result into payload
                return request.getInvocationType().equals(InvocationType.RequestResponse.name()) ?
                        new InvokeResult().withPayload(ByteBuffer.wrap(outputStream.toByteArray())) : new InvokeResult();
            }
            return invocationOnMock.callRealMethod();
        });
    }

    private RequestStreamHandler givenHandler() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStandardCard();
        return AssetFactory.givenRequestStreamHandlerThatReturns(envelope);
    }

    private AlexaClient givenClient(final String script) throws Exception {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(script);
        final AlexaClient.AlexaClientBuilder builder = AlexaClient.create(stream);

        // if endpoint is Lambda replace endpoint with Mock
        if (builder.endpoint instanceof AlexaLambdaEndpoint) {
            final String lambdaFunctionName = ((AlexaLambdaEndpoint)builder.endpoint).getLambdaFunctionName();
            final AlexaLambdaEndpoint endpoint = AlexaLambdaEndpoint.create(lambdaFunctionName)
                    .withLambdaClient(givenLambdaMock()).build();
            builder.withEndpoint(endpoint);
        } else if (builder.endpoint instanceof AlexaRequestStreamHandlerEndpoint) {
            final AlexaRequestStreamHandlerEndpoint endpoint = AlexaRequestStreamHandlerEndpoint.create(givenHandler()).build();
            builder.withEndpoint(endpoint);
        }
        return builder.build();
    }

    @Test
    public void generateUserId() throws Exception {
        final String userId = AlexaClient.generateUserId();
        Assert.assertNotNull(userId);
        Assert.assertTrue(userId.matches("amzn1.ask.account.[A-Z0-9-]{207}"));

        final String userId2 = AlexaClient.generateUserId();
        Assert.assertNotEquals(userId, userId2);
    }

    @Test
    public void generateApplicationId() throws Exception {
        final String applicationId = AlexaClient.generateApplicationId();
        Assert.assertNotNull(applicationId);
        Assert.assertTrue(applicationId.matches("amzn1.ask.skill.[a-z0-9-]{36}"));

        final String applicationId2 = AlexaClient.generateApplicationId();
        Assert.assertNotEquals(applicationId, applicationId2);
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final AlexaEndpoint endpoint = AssetFactory.givenRequestStreamHandlerEndpoint();
        final AlexaClient test1 = AlexaClient.create(endpoint).build();
        Assert.assertEquals(test1.getLocale(), Locale.US);
        Assert.assertEquals(test1.getEndpoint(), endpoint);
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final AlexaEndpoint endpoint = AssetFactory.givenRequestStreamHandlerEndpoint();
        final Date timestamp = DateUtils.addDays(new Date(), 5);
        final AlexaClient test1 = AlexaClient
                .create(endpoint, "appId")
                .withLocale(Locale.GERMANY)
                .withUserId("uid")
                .withTimestamp(timestamp)
                .withAccessToken("accessToken")
                .withDebugFlagSessionAttribute("debug123flag")
                .build();

        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getEndpoint(), endpoint);
        Assert.assertEquals(test1.getDebugFlagSessionAttributeName(), Optional.of("debug123flag"));
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
        Assert.assertEquals(test1.getUser().getUserId(), "uid");
        Assert.assertEquals(test1.getUser().getAccessToken(), "accessToken");
        Assert.assertTrue(DateUtils.isSameDay(timestamp, test1.getCurrentTimestamp()));
    }

    @Test
    public void createFromScriptFile() throws Exception {
        final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("script-max.xml");
        final AlexaClient client = AlexaClient.create(stream).build();

        Assert.assertEquals(client.getLocale(), Locale.GERMANY);
        Assert.assertTrue(client.getEndpoint() instanceof AlexaLambdaEndpoint);
        Assert.assertEquals(((AlexaLambdaEndpoint)client.getEndpoint()).getLambdaFunctionName(), "myLambdaFunctionName");
        Assert.assertEquals(client.getDebugFlagSessionAttributeName(), Optional.of("myDebugFlag"));
        Assert.assertEquals(client.getApplication().getApplicationId(), "myApplicationId");
        Assert.assertEquals(client.getUser().getUserId(), "myUserId");
        Assert.assertEquals(client.getUser().getAccessToken(), "myAccessToken");
        Assert.assertTrue(DateUtils.isSameDay(client.getCurrentTimestamp(), new GregorianCalendar(2010, 8, 30).getTime()));
    }

    @Test
    public void startSessionWithLambdaEndpoint() throws Exception {
        startSession("script-lambdaFunction.xml");
    }

    @Test
    public void startScriptWithLambdaEndpoint() throws Exception {
        startScript("script-lambdaFunction.xml");
    }

    @Test
    public void startSessionWithRequestStreamHandlerEndpoint() throws Exception {
        startSession("script-requestStreamHandler.xml");
    }

    @Test
    public void startScriptWithRequestStreamHandlerEndpoint() throws Exception {
        startScript("script-requestStreamHandler.xml");
    }

    @Test
    public void startScriptWithLambdaEndpointDeep() throws Exception {
        startScript("script-deep.xml");
    }

    private void startScript(final String scriptFile) throws Exception {
        givenClient(scriptFile).startScript();
    }

    private void startSession(final String scriptFile) throws Exception {
        givenClient(scriptFile).startSession()
                .launch()
                    .assertSessionStillOpen()
                    .assertSessionStateExists("myDebugFlag")
                    .assertTrue(AlexaAssertion.HasCard)
                    .assertExecutionTimeLessThan(1000)
                    .done()
                .intent("myIntent", "slot1", true)
                    .assertSessionStillOpen()
                    .assertSessionStateEquals("slot1", "true")
                    .assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "myIntent")
                    .assertMatches(AlexaAsset.OutputSpeechSsml, ".*" + AssetFactory.DEFAULT_TEXT +".*")
                    .done()
                .intent("myIntent2", "slot2", 123)
                    .assertSessionStillOpen()
                    .assertSessionStateEquals("slot1", "true")
                    .assertSessionStateEquals("slot2", "123")
                    .assertFalse(AlexaAssertion.HasDirective)
                    .assertMatches(AlexaAsset.RepromptSpeechSsml, ".*" + AssetFactory.DEFAULT_TEXT +".*")
                    .done()
                .intent("myIntent3", "slot2", 321)
                    .assertSessionStillOpen()
                    .assertSessionStateEquals("slot2", "321")
                    .done()
                .delay(1000)
                .repeat()
                    .assertThat(e -> e.getVersion().equals(AlexaClient.VERSION))
                    .assertNotEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_SMALL_IMAGE)
                    .assertEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_LARGE_IMAGE)
                    .assertSessionStateExists("myDebugFlag")
                    .done()
                .endSession();
    }
}