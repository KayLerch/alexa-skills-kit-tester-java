package io.klerch.alexa.tester.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.klerch.alexa.tester.AssetFactory;
import io.klerch.alexa.tester.asset.AlexaAssertion;
import io.klerch.alexa.tester.asset.AlexaAsset;
import io.klerch.alexa.tester.client.AlexaUnitClient;
import io.klerch.alexa.tester.request.AlexaIntentRequest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class AlexaResponseTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void isEmpty() throws Exception {

    }

    private AlexaResponse givenResponse() throws Exception {
        return givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech());
    }

    private AlexaResponse givenResponse(final SpeechletResponseEnvelope envelope) throws Exception {
        final AlexaIntentRequest intentRequest = new AlexaIntentRequest(AssetFactory.givenActor(), "intent");
        return new AlexaResponse(intentRequest, envelope.toJsonBytes());
    }

    @Test
    public void assertThat() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertThat(AlexaAssertion.SessionStillOpen));
        Assert.assertEquals(response, response.assertThat(AlexaAssertion.HasOutputSpeech));
        Assert.assertEquals(response, response.assertThat(AlexaAssertion.HasOutputSpeechIsSsml));
        Assert.assertEquals(response, response.assertThat(AlexaAssertion.HasRepromptSpeech));
        Assert.assertEquals(response, response.assertThat(AlexaAssertion.HasRepromptSpeechIsSsml));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertThat(AlexaAssertion.HasCard));
    }

    @Test
    public void assertTrue() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.SessionStillOpen));
        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.HasOutputSpeech));
        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.HasOutputSpeechIsSsml));
        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.HasRepromptSpeech));
        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.HasRepromptSpeechIsSsml));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertTrue(AlexaAssertion.HasCard));
    }

    @Test
    public void assertFalse() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.SessionEnded));
        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.HasCard));
        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.HasDirective));
        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.HasCardIsSimple));
        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertFalse(AlexaAssertion.SessionStillOpen));
    }

    @Test
    public void assertExists() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertExists(AlexaAsset.OutputSpeech));
        Assert.assertEquals(response, response.assertExists(AlexaAsset.OutputSpeechSsml));
        Assert.assertEquals(response, response.assertExists(AlexaAsset.RepromptSpeech));
        Assert.assertEquals(response, response.assertExists(AlexaAsset.RepromptSpeechSsml));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertExists(AlexaAsset.Card));
    }

    @Test
    public void assertNotExists() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertNotExists(AlexaAsset.Card));
        Assert.assertEquals(response, response.assertNotExists(AlexaAsset.DirectivePlay));
        Assert.assertEquals(response, response.assertNotExists(AlexaAsset.SimpleCard));
        Assert.assertEquals(response, response.assertNotExists(AlexaAsset.DirectivePlayAudioItemStreamOffset));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertNotExists(AlexaAsset.OutputSpeech));
    }

    @Test
    public void assertEquals() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertEquals(AlexaAsset.OutputSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
        Assert.assertEquals(response, response.assertEquals(AlexaAsset.RepromptSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertEquals(AlexaAsset.OutputSpeech, AssetFactory.DEFAULT_TEXT));
    }

    @Test
    public void assertNotEquals() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertEquals(response, response.assertNotEquals(AlexaAsset.OutputSpeech, AssetFactory.DEFAULT_TEXT));
        Assert.assertEquals(response, response.assertNotEquals(AlexaAsset.Card, ""));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertNotEquals(AlexaAsset.OutputSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
    }

    @Test
    public void assertSessionEnded() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech("text", true));
        Assert.assertEquals(response, response.assertSessionEnded());

        final AlexaResponse response2 = givenResponse();
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response2, response2.assertSessionEnded());
    }

    @Test
    public void assertSessionStillOpen() throws Exception {
        final AlexaResponse response = givenResponse();
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStillOpen());

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech("text", true));
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response2, response2.assertSessionStillOpen());
    }

    @Test
    public void getShouldEndSession() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech("text", true));
        Assert.assertTrue(response.getShouldEndSession());

        final AlexaResponse response2 = givenResponse();
        Assert.assertFalse(response2.getShouldEndSession());
    }

    @Test
    public void getSessionAttributes() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertNotNull(response.getSessionAttributes());
        Assert.assertEquals(1, response.getSessionAttributes().size());
        Assert.assertTrue(response.getSessionAttributes().containsKey("attr"));
    }

    @Test
    public void assertSessionStateExists() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateExists("attr"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateExists("attr1"));
    }

    @Test
    public void assertSessionStateNotNull() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateNotNull("attr"));

        exception.expect(NullPointerException.class);
        Assert.assertEquals(response, response.assertSessionStateNotNull("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", null));
        exception.expect(NullPointerException.class);
        Assert.assertEquals(response2, response2.assertSessionStateNotNull("attr"));
    }

    @Test
    public void assertSessionStateNotBlank() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateNotBlank("attr"));

        exception.expect(NullPointerException.class);
        Assert.assertEquals(response, response.assertSessionStateNotBlank("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", ""));
        exception.expect(NullPointerException.class);
        Assert.assertEquals(response2, response2.assertSessionStateNotBlank("attr"));
    }

    @Test
    public void assertSessionStateEquals() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateEquals("attr", "true"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateEquals("attr", "123"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertEquals(response2, response2.assertSessionStateEquals("attr", "321"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 32.61));
        Assert.assertEquals(response3, response3.assertSessionStateEquals("attr", "32.61"));
    }

    @Test
    public void assertSessionStateContains() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", "my string"));
        Assert.assertEquals(response, response.assertSessionStateContains("attr", "y str"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateContains("attr", "Y str"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertEquals(response2, response2.assertSessionStateContains("attr", "3"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response3, response3.assertSessionStateContains("attr", "true"));
    }

    @Test
    public void assertSessionStateMatches() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", "my string"));
        Assert.assertEquals(response, response.assertSessionStateMatches("attr", ".*my s.*"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateMatches("attr", "[A-Z]*"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertEquals(response2, response2.assertSessionStateMatches("attr", "[1-3]*"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response3, response3.assertSessionStateMatches("attr", "true"));
    }
}