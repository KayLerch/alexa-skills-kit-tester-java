package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import org.junit.Assert;
import org.junit.Test;

public abstract class AlexaClientTest {
    public abstract AlexaClient givenClient() throws Exception;

    RequestStreamHandler givenHandler() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStandardCard();
        return AssetFactory.givenRequestStreamHandlerThatReturns(envelope);
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
    public void doConversation() throws Exception {
        givenClient().startSession()
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

    Context givenContext() {
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