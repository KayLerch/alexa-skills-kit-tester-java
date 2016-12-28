package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
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
    public void doConversation() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStandardCard();
        final RequestStreamHandler handler = AssetFactory.givenRequestStreamHandlerThatReturns(envelope);
        final AlexaUnitClient client = AlexaUnitClient
                .create(AlexaClient.generateApplicationId(), handler)
                .withDebugFlagSessionAttribute("myDebugFlag")
                .build();

        client.startSession()
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
                .repeat()
                    .assertThat(e -> e.getVersion().equals(AlexaClient.VERSION))
                    .assertNotEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_SMALL_IMAGE)
                    .assertEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_LARGE_IMAGE)
                    .assertSessionStateExists("myDebugFlag")
                    .done()
                .endSession();
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