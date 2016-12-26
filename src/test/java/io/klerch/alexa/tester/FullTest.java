package io.klerch.alexa.tester;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.klerch.alexa.tester.asset.AlexaAssertion;
import io.klerch.alexa.tester.asset.AlexaAsset;
import io.klerch.alexa.tester.client.AlexaUnitClient;
import org.junit.Test;

public class FullTest {
    @Test
    public void doConversation() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStandardCard();
        final RequestStreamHandler handler = AssetFactory.givenRequestStreamHandlerThatReturns(envelope);
        final AlexaUnitClient client = AlexaUnitClient.create("appId", handler).build();

        client.startSession()
                .launch()
                    .assertSessionStillOpen()
                    .assertTrue(AlexaAssertion.HasCard)
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
                    .assertThat(AlexaAssertion.HasCardIsStandard)
                    .assertNotEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_SMALL_IMAGE)
                    .assertEquals(AlexaAsset.StandardCardLargeImageUrl, AssetFactory.DEFAULT_CARD_LARGE_IMAGE)
                    .done()
                .endSession();
    }
}
