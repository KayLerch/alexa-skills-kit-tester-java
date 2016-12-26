package io.klerch.alexa.tester.client;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.tester.AssetFactory;
import io.klerch.alexa.tester.request.AlexaIntentRequest;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Locale;

public class AlexaUnitClientTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void createInvalid() throws Exception {
        exception.expect(NullPointerException.class);
        AlexaUnitClient.create("appId", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitClient.create("", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitClient.create(null, AssetFactory.getRequestStreamHandler()).build();
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final RequestStreamHandler handler = AssetFactory.getRequestStreamHandler();
        final AlexaUnitClient test1 = AlexaUnitClient.create("appId", handler).build();
        Assert.assertEquals(test1.getLocale(), Locale.US);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertNotNull(test1.getContext());
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final RequestStreamHandler handler = AssetFactory.getRequestStreamHandler();
        final Context context = getContext();
        final AlexaUnitClient test1 = AlexaUnitClient.create("appId", handler)
                .withLocale(Locale.GERMANY)
                .withUserId("uid")
                .withContext(context)
                .withAccessToken("accessToken")
                .build();
        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertEquals(test1.getContext(), context);
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
        Assert.assertEquals(test1.getUser().getUserId(), "uid");
        Assert.assertEquals(test1.getUser().getAccessToken(), "accessToken");
    }

    @Test
    public void fire() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSessionAttribute("attr", true);
        final RequestStreamHandler handler = AssetFactory.givenRequestStreamHandlerThatReturns(envelope);
        final AlexaUnitClient client = AlexaUnitClient.create("appId", handler).build();
        //final AlexaResponse response = client.fire(AssetFactory.givenRe)
    }

    private Context getContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }
}