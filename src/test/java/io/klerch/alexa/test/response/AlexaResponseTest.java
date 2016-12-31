package io.klerch.alexa.test.response;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.ui.SsmlOutputSpeech;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.request.AlexaIntentRequest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AlexaResponseTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

        Assert.assertEquals(response, response.assertThat(e -> !e.getResponse().getShouldEndSession()));
        Assert.assertEquals(response, response.assertThat(e -> e.getResponse() != null));
        Assert.assertEquals(response, response.assertThat(e -> e.getResponse().getOutputSpeech() != null));
        Assert.assertEquals(response, response.assertThat(e -> e.getResponse().getOutputSpeech() instanceof SsmlOutputSpeech));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertThat(e -> e.getResponse().getShouldEndSession()));
    }

    @Test
    public void is() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.is(e -> !e.getResponse().getShouldEndSession()));
        Assert.assertTrue(response.is(e -> e.getResponse() != null));
        Assert.assertTrue(response.is(e -> e.getResponse().getOutputSpeech() != null));
        Assert.assertTrue(response.is(e -> e.getResponse().getOutputSpeech() instanceof SsmlOutputSpeech));

        Assert.assertFalse(response.is(e -> e.getResponse().getShouldEndSession()));
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
    public void isTrue() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.isTrue(AlexaAssertion.SessionStillOpen));
        Assert.assertTrue(response.isTrue(AlexaAssertion.HasOutputSpeech));
        Assert.assertTrue(response.isTrue(AlexaAssertion.HasOutputSpeechIsSsml));
        Assert.assertTrue(response.isTrue(AlexaAssertion.HasRepromptSpeech));
        Assert.assertTrue(response.isTrue(AlexaAssertion.HasRepromptSpeechIsSsml));

        Assert.assertFalse(response.isTrue(AlexaAssertion.HasCard));
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
    public void isFalse() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.isFalse(AlexaAssertion.SessionEnded));
        Assert.assertTrue(response.isFalse(AlexaAssertion.HasCard));
        Assert.assertTrue(response.isFalse(AlexaAssertion.HasDirective));
        Assert.assertTrue(response.isFalse(AlexaAssertion.HasCardIsSimple));
        Assert.assertTrue(response.isFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll));

        Assert.assertFalse(response.isFalse(AlexaAssertion.SessionStillOpen));
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
    public void exists() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.exists(AlexaAsset.OutputSpeech));
        Assert.assertTrue(response.exists(AlexaAsset.OutputSpeechSsml));
        Assert.assertTrue(response.exists(AlexaAsset.RepromptSpeech));
        Assert.assertTrue(response.exists(AlexaAsset.RepromptSpeechSsml));

        Assert.assertFalse(response.exists(AlexaAsset.Card));
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
    public void notExists() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.notExists(AlexaAsset.Card));
        Assert.assertTrue(response.notExists(AlexaAsset.DirectivePlay));
        Assert.assertTrue(response.notExists(AlexaAsset.SimpleCard));
        Assert.assertTrue(response.notExists(AlexaAsset.DirectivePlayAudioItemStreamOffset));

        Assert.assertFalse(response.notExists(AlexaAsset.OutputSpeech));
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
    public void equals() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.equals(AlexaAsset.OutputSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
        Assert.assertTrue(response.equals(AlexaAsset.RepromptSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));

        Assert.assertFalse(response.equals(AlexaAsset.OutputSpeech, AssetFactory.DEFAULT_TEXT));
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
    public void notEquals() throws Exception {
        final AlexaResponse response = givenResponse();

        Assert.assertTrue(response.notEquals(AlexaAsset.OutputSpeech, AssetFactory.DEFAULT_TEXT));
        Assert.assertTrue(response.notEquals(AlexaAsset.Card, ""));

        Assert.assertFalse(response.notEquals(AlexaAsset.OutputSpeechSsml, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
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
    public void sessionEnded() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech("text", true));
        Assert.assertTrue(response.sessionEnded());

        final AlexaResponse response2 = givenResponse();
        Assert.assertFalse(response2.sessionEnded());
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
    public void sessionOpen() throws Exception {
        final AlexaResponse response = givenResponse();
        Assert.assertTrue(response.sessionStillOpen());

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSsmlOutputSpeech("text", true));
        Assert.assertFalse(response2.sessionStillOpen());
    }

    @Test
    public void getResponseEnvelope() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertNotNull(response.getResponseEnvelope());
    }

    @Test
    public void assertSessionStateExists() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateExists("attr"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateExists("attr1"));
    }

    @Test
    public void sessionStateExists() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response.sessionStateExists("attr"));

        Assert.assertFalse(response.sessionStateExists("attr1"));
    }

    @Test
    public void assertSessionStateNotNull() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateNotNull("attr"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateNotNull("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", null));
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response2, response2.assertSessionStateNotNull("attr"));
    }

    @Test
    public void sessionStateNotNull() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response.sessionStateNotNull("attr"));

        Assert.assertFalse(response.sessionStateNotNull("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", null));
        Assert.assertFalse(response2.sessionStateNotNull("attr"));
    }

    @Test
    public void assertSessionStateNotBlank() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertEquals(response, response.assertSessionStateNotBlank("attr"));

        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response, response.assertSessionStateNotBlank("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", ""));
        exception.expect(IllegalArgumentException.class);
        Assert.assertEquals(response2, response2.assertSessionStateNotBlank("attr"));
    }

    @Test
    public void sessionStateNotBlank() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response.sessionStateNotBlank("attr"));

        Assert.assertFalse(response.sessionStateNotBlank("attr1"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", ""));
        Assert.assertFalse(response2.sessionStateNotBlank("attr"));
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
    public void sessionStateEquals() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response.sessionStateEquals("attr", "true"));

        Assert.assertFalse(response.sessionStateEquals("attr", "123"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertTrue(response2.sessionStateEquals("attr", "321"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 32.61));
        Assert.assertTrue(response3.sessionStateEquals("attr", "32.61"));
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
    public void sessionStateContains() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", "my string"));
        Assert.assertTrue(response.sessionStateContains("attr", "y str"));

        Assert.assertFalse(response.sessionStateContains("attr", "Y str"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertTrue(response2.sessionStateContains("attr", "3"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response3.sessionStateContains("attr", "true"));
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

    @Test
    public void sessionStateMatches() throws Exception {
        final AlexaResponse response = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", "my string"));
        Assert.assertTrue(response.sessionStateMatches("attr", ".*my s.*"));

        Assert.assertFalse(response.sessionStateMatches("attr", "[A-Z]*"));

        final AlexaResponse response2 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", 321));
        Assert.assertTrue(response2.sessionStateMatches("attr", "[1-3]*"));

        final AlexaResponse response3 = givenResponse(AssetFactory.givenResponseWithSessionAttribute("attr", true));
        Assert.assertTrue(response3.sessionStateMatches("attr", "true"));
    }
}