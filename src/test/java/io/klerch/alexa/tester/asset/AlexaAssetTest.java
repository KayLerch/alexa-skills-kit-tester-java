package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.ui.StandardCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

public class AlexaAssetTest {
    @Test
    public void simpleCardExists() throws Exception {
        // with correct card
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSimpleCard();
        Assert.assertTrue(AlexaAsset.SimpleCard.exists(envelope));
        Assert.assertTrue(AlexaAsset.SimpleCardTitle.exists(envelope));
        Assert.assertTrue(AlexaAsset.SimpleCardContent.exists(envelope));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithStandardCard();
        Assert.assertFalse(AlexaAsset.SimpleCard.exists(envelope2));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.exists(envelope2));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.exists(envelope2));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.SimpleCard.exists(envelope3));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.exists(envelope3));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.exists(envelope3));
    }

    @Test
    public void simpleCardEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSimpleCard();
        final ObjectMapper mapper = new ObjectMapper();
        final String cardJson = mapper.writeValueAsString(envelope.getResponse().getCard());
        // with correct card
        Assert.assertTrue(AlexaAsset.SimpleCard.equals(envelope, cardJson));
        Assert.assertTrue(AlexaAsset.SimpleCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertTrue(AlexaAsset.SimpleCardContent.equals(envelope, AssetFactory.DEFAULT_CARD_CONTENT));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE + " "));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.equals(envelope, AssetFactory.DEFAULT_CARD_CONTENT + " "));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithStandardCard();
        Assert.assertFalse(AlexaAsset.SimpleCard.equals(envelope2, cardJson));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.equals(envelope2, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.equals(envelope2, AssetFactory.DEFAULT_CARD_CONTENT));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.SimpleCard.equals(envelope3, cardJson));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.equals(envelope3, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.equals(envelope3, AssetFactory.DEFAULT_CARD_CONTENT));
    }

    @Test
    public void simpleCardMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSimpleCard();
        final String patternPositive = ".*" + AssetFactory.DEFAULT_CARD_TITLE +
                ".*|.*" + AssetFactory.DEFAULT_CARD_CONTENT + ".*";
        final String patternNegative = "[abc]";

        // with correct card
        Assert.assertTrue(AlexaAsset.SimpleCard.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.SimpleCardTitle.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.SimpleCardContent.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.SimpleCard.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.matches(envelope, patternNegative));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard("another title", "another content");
        Assert.assertFalse(AlexaAsset.SimpleCard.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.matches(envelope2, patternPositive));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.SimpleCard.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.SimpleCardTitle.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.SimpleCardContent.matches(envelope3, patternPositive));
    }

    @Test
    public void standardCardExists() throws Exception {
        // with correct card
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithStandardCard();
        Assert.assertTrue(AlexaAsset.StandardCard.exists(envelope));
        Assert.assertTrue(AlexaAsset.StandardCardTitle.exists(envelope));
        Assert.assertTrue(AlexaAsset.StandardCardText.exists(envelope));
        Assert.assertTrue(AlexaAsset.StandardCardImage.exists(envelope));
        Assert.assertTrue(AlexaAsset.StandardCardLargeImageUrl.exists(envelope));
        Assert.assertTrue(AlexaAsset.StandardCardSmallImageUrl.exists(envelope));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard();
        Assert.assertFalse(AlexaAsset.StandardCard.exists(envelope2));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.exists(envelope2));
        Assert.assertFalse(AlexaAsset.StandardCardText.exists(envelope2));
        Assert.assertFalse(AlexaAsset.StandardCardImage.exists(envelope2));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.exists(envelope2));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.exists(envelope2));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.StandardCard.exists(envelope3));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.exists(envelope3));
        Assert.assertFalse(AlexaAsset.StandardCardText.exists(envelope3));
        Assert.assertFalse(AlexaAsset.StandardCardImage.exists(envelope3));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.exists(envelope3));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.exists(envelope3));
    }

    @Test
    public void standardCardEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithStandardCard();
        final ObjectMapper mapper = new ObjectMapper();
        final String cardJson = mapper.writeValueAsString(envelope.getResponse().getCard());
        final String imageJson = mapper.writeValueAsString(((StandardCard)envelope.getResponse().getCard()).getImage());
        // with correct card
        Assert.assertTrue(AlexaAsset.StandardCard.equals(envelope, cardJson));
        Assert.assertTrue(AlexaAsset.StandardCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertTrue(AlexaAsset.StandardCardText.equals(envelope, AssetFactory.DEFAULT_CARD_TEXT));
        Assert.assertTrue(AlexaAsset.StandardCardImage.equals(envelope, imageJson));
        Assert.assertTrue(AlexaAsset.StandardCardSmallImageUrl.equals(envelope, AssetFactory.DEFAULT_CARD_SMALL_IMAGE));
        Assert.assertTrue(AlexaAsset.StandardCardLargeImageUrl.equals(envelope, AssetFactory.DEFAULT_CARD_LARGE_IMAGE));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE + " "));
        Assert.assertFalse(AlexaAsset.StandardCardText.equals(envelope, AssetFactory.DEFAULT_CARD_TEXT + " "));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.equals(envelope, AssetFactory.DEFAULT_CARD_SMALL_IMAGE + " "));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.equals(envelope, AssetFactory.DEFAULT_CARD_LARGE_IMAGE + " "));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard();
        Assert.assertFalse(AlexaAsset.StandardCard.equals(envelope2, cardJson));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.equals(envelope2, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertFalse(AlexaAsset.StandardCardText.equals(envelope2, AssetFactory.DEFAULT_CARD_CONTENT));
        Assert.assertFalse(AlexaAsset.StandardCardImage.equals(envelope2, imageJson));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.equals(envelope2, AssetFactory.DEFAULT_CARD_SMALL_IMAGE));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.equals(envelope2, AssetFactory.DEFAULT_CARD_LARGE_IMAGE));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.StandardCard.equals(envelope3, cardJson));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.equals(envelope3, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertFalse(AlexaAsset.StandardCardText.equals(envelope3, AssetFactory.DEFAULT_CARD_CONTENT));
        Assert.assertFalse(AlexaAsset.StandardCardImage.equals(envelope3, imageJson));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.equals(envelope3, AssetFactory.DEFAULT_CARD_SMALL_IMAGE));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.equals(envelope3, AssetFactory.DEFAULT_CARD_LARGE_IMAGE));
    }

    @Test
    public void standardCardMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithStandardCard();
        final String patternPositive = ".*" + AssetFactory.DEFAULT_CARD_TITLE +
                ".*|.*" + AssetFactory.DEFAULT_CARD_TEXT + ".*" +
                ".*|.*" + AssetFactory.DEFAULT_CARD_SMALL_IMAGE + ".*" +
                ".*|.*" + AssetFactory.DEFAULT_CARD_LARGE_IMAGE + ".*";
        final String patternNegative = "[abc]";

        // with correct card
        Assert.assertTrue(AlexaAsset.StandardCard.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.StandardCardTitle.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.StandardCardText.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.StandardCardImage.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.StandardCardSmallImageUrl.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.StandardCardLargeImageUrl.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCard.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.StandardCardText.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.StandardCardImage.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.matches(envelope, patternNegative));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard("another title", "another content");
        Assert.assertFalse(AlexaAsset.StandardCard.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardText.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardImage.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.matches(envelope2, patternPositive));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.StandardCard.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardTitle.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardText.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardImage.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardSmallImageUrl.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.StandardCardLargeImageUrl.matches(envelope3, patternPositive));
    }

    @Test
    public void linkAccountCardExists() throws Exception {
        // with correct card
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithLinkAccountCard();
        Assert.assertTrue(AlexaAsset.LinkAccountCard.exists(envelope));
        Assert.assertTrue(AlexaAsset.LinkAccountCardTitle.exists(envelope));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard();
        Assert.assertFalse(AlexaAsset.LinkAccountCard.exists(envelope2));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.exists(envelope2));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.LinkAccountCard.exists(envelope3));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.exists(envelope3));
    }

    @Test
    public void linkAccountCardEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithLinkAccountCard();
        final ObjectMapper mapper = new ObjectMapper();
        final String cardJson = mapper.writeValueAsString(envelope.getResponse().getCard());
        // with correct card
        Assert.assertTrue(AlexaAsset.LinkAccountCard.equals(envelope, cardJson));
        Assert.assertTrue(AlexaAsset.LinkAccountCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE + " "));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithStandardCard();
        Assert.assertFalse(AlexaAsset.LinkAccountCard.equals(envelope2, cardJson));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.equals(envelope2, AssetFactory.DEFAULT_CARD_TITLE));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.LinkAccountCard.equals(envelope3, cardJson));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.equals(envelope3, AssetFactory.DEFAULT_CARD_TITLE));
    }

    @Test
    public void linkAccountCardMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithLinkAccountCard();
        final String patternPositive = ".*" + AssetFactory.DEFAULT_CARD_TITLE + ".*";
        final String patternNegative = "[abc]";

        // with correct card
        Assert.assertTrue(AlexaAsset.LinkAccountCard.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.LinkAccountCardTitle.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.LinkAccountCard.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.matches(envelope, patternNegative));
        // with another card
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithSimpleCard("another title", "another content");
        Assert.assertFalse(AlexaAsset.LinkAccountCard.matches(envelope2, patternPositive));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.matches(envelope2, patternPositive));
        // without card
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAsset.LinkAccountCard.matches(envelope3, patternPositive));
        Assert.assertFalse(AlexaAsset.LinkAccountCardTitle.matches(envelope3, patternPositive));
    }

    @Test
    public void outputSpeechExists() throws Exception {
        // with ssml
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSsmlOutputSpeech();
        Assert.assertTrue(AlexaAsset.OutputSpeech.exists(envelope));
        Assert.assertTrue(AlexaAsset.OutputSpeechSsml.exists(envelope));
        Assert.assertFalse(AlexaAsset.OutputSpeechPlainText.exists(envelope));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.exists(envelope));
        Assert.assertTrue(AlexaAsset.RepromptSpeechSsml.exists(envelope));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.exists(envelope));
        // with plain
        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithPlainOutputSpeech();
        Assert.assertTrue(AlexaAsset.OutputSpeech.exists(envelope2));
        Assert.assertTrue(AlexaAsset.OutputSpeechPlainText.exists(envelope2));
        Assert.assertFalse(AlexaAsset.OutputSpeechSsml.exists(envelope2));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.exists(envelope2));
        Assert.assertTrue(AlexaAsset.RepromptSpeechPlainText.exists(envelope2));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.exists(envelope2));
        // without reprompt
        final SpeechletResponseEnvelope envelope3 = AssetFactory.getResponseWithSsmlOutputSpeech("text", true);
        Assert.assertFalse(AlexaAsset.RepromptSpeech.exists(envelope3));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.exists(envelope3));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.exists(envelope3));
    }

    @Test
    public void outputSpeechSsmlEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSsmlOutputSpeech();
        final ObjectMapper mapper = new ObjectMapper();
        final String speechJson = mapper.writeValueAsString(envelope.getResponse().getOutputSpeech());
        final String repromptJson = mapper.writeValueAsString(envelope.getResponse().getReprompt().getOutputSpeech());

        Assert.assertTrue(AlexaAsset.OutputSpeech.equals(envelope, speechJson));
        Assert.assertTrue(AlexaAsset.OutputSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
        Assert.assertFalse(AlexaAsset.OutputSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + " </speak>"));
        Assert.assertFalse(AlexaAsset.OutputSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE + " "));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.equals(envelope, repromptJson));
        Assert.assertTrue(AlexaAsset.RepromptSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + " </speak>"));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_CARD_TITLE + " "));
    }

    @Test
    public void outputSpeechPlainEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithPlainOutputSpeech();
        final ObjectMapper mapper = new ObjectMapper();
        final String speechJson = mapper.writeValueAsString(envelope.getResponse().getOutputSpeech());
        final String repromptJson = mapper.writeValueAsString(envelope.getResponse().getReprompt().getOutputSpeech());

        Assert.assertTrue(AlexaAsset.OutputSpeech.equals(envelope, speechJson));
        Assert.assertTrue(AlexaAsset.OutputSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT));
        Assert.assertFalse(AlexaAsset.OutputSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT + " "));
        Assert.assertFalse(AlexaAsset.OutputSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.equals(envelope, repromptJson));
        Assert.assertTrue(AlexaAsset.RepromptSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT + " "));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
    }

    @Test
    public void outputSpeechWithoutRepromptEquals() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithPlainOutputSpeech("text", true);
        final ObjectMapper mapper = new ObjectMapper();
        final String speechJson = mapper.writeValueAsString(envelope.getResponse().getOutputSpeech());

        Assert.assertFalse(AlexaAsset.RepromptSpeech.equals(envelope, speechJson));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, AssetFactory.DEFAULT_TEXT + " "));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.equals(envelope, "<speak>" + AssetFactory.DEFAULT_TEXT + "</speak>"));
    }

    @Test
    public void outputSpeechSsmlMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithSsmlOutputSpeech();
        final String patternPositive = ".*" + AssetFactory.DEFAULT_TEXT + ".*";
        final String patternNegative = "[abc]";

        Assert.assertTrue(AlexaAsset.OutputSpeech.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.OutputSpeechSsml.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.OutputSpeechSsml.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.OutputSpeechPlainText.equals(envelope, patternPositive));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.RepromptSpeechSsml.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.matches(envelope, patternPositive + " "));
    }

    @Test
    public void outputSpeechPlainMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithPlainOutputSpeech();
        final String patternPositive = ".*" + AssetFactory.DEFAULT_TEXT + ".*";
        final String patternNegative = "[abc]";

        Assert.assertTrue(AlexaAsset.OutputSpeech.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.OutputSpeechPlainText.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.OutputSpeechPlainText.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.OutputSpeechSsml.matches(envelope, patternPositive));

        Assert.assertTrue(AlexaAsset.RepromptSpeech.matches(envelope, patternPositive));
        Assert.assertTrue(AlexaAsset.RepromptSpeechPlainText.matches(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.matches(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.matches(envelope, patternPositive));
    }

    @Test
    public void outputSpeechWithoutRepromptMatches() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithPlainOutputSpeech("text", true);
        final String patternPositive = ".*" + AssetFactory.DEFAULT_TEXT + ".*";
        final String patternNegative = "[abc]";

        Assert.assertFalse(AlexaAsset.RepromptSpeech.equals(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, patternPositive));
        Assert.assertFalse(AlexaAsset.RepromptSpeechPlainText.equals(envelope, patternNegative));
        Assert.assertFalse(AlexaAsset.RepromptSpeechSsml.equals(envelope, patternPositive));
    }

    @Test
    public void directivePlayExists() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithPlayDirective(PlayBehavior.REPLACE_ALL);

        Assert.assertTrue(AlexaAsset.DirectivePlay.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectiveStop.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectiveClearQueue.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectiveClearQueueBehavior.exists(envelope));

        Assert.assertTrue(AlexaAsset.DirectivePlay.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayBehavior.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItem.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItemStream.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItemStreamOffset.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItemStreamPreviousToken.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItemStreamToken.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectivePlayAudioItemStreamUrl.exists(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.getResponseWithPlayDirective(PlayBehavior.REPLACE_ALL, false);
        Assert.assertTrue(AlexaAsset.DirectivePlay.exists(envelope2));
        Assert.assertTrue(AlexaAsset.DirectivePlayBehavior.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItem.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItemStream.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItemStreamOffset.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItemStreamPreviousToken.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItemStreamToken.exists(envelope2));
        Assert.assertFalse(AlexaAsset.DirectivePlayAudioItemStreamUrl.exists(envelope2));
    }

    @Test
    public void directiveStopExists() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithStopDirective();

        Assert.assertTrue(AlexaAsset.DirectiveStop.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectivePlay.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectiveClearQueue.exists(envelope));
    }

    @Test
    public void directiveClearQueueExists() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.getResponseWithClearQueueDirective(ClearBehavior.CLEAR_ALL);

        Assert.assertTrue(AlexaAsset.DirectiveClearQueue.exists(envelope));
        Assert.assertTrue(AlexaAsset.DirectiveClearQueueBehavior.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectivePlay.exists(envelope));
        Assert.assertFalse(AlexaAsset.DirectiveStop.exists(envelope));
    }
}