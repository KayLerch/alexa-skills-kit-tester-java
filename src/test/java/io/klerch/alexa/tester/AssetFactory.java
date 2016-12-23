package io.klerch.alexa.tester;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.*;

public class AssetFactory {
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String DEFAULT_TEXT = "test";
    public static final String DEFAULT_CARD_TITLE = "card_title";
    public static final String DEFAULT_CARD_CONTENT = "card_content";
    public static final String DEFAULT_CARD_TEXT = "card_text";
    public static final String DEFAULT_CARD_SMALL_IMAGE = "https://img/small.jpg";
    public static final String DEFAULT_CARD_LARGE_IMAGE = "https://img/large.jpg";

    public static SpeechletResponseEnvelope getResponseWithSsmlOutputSpeech() {
        return getResponseWithSsmlOutputSpeech(DEFAULT_TEXT);
    }

    public static SpeechletResponseEnvelope getResponseWithSsmlOutputSpeech(final String ssml) {
        return getResponseWithSsmlOutputSpeech(ssml, false);
    }

    public static SpeechletResponseEnvelope getResponseWithSsmlOutputSpeech(final String ssml, final boolean tell) {
        final SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml(String.format("<speak>%s</speak>", ssml));
        final Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);
        return envelope(tell ? SpeechletResponse.newTellResponse(outputSpeech) : SpeechletResponse.newAskResponse(outputSpeech, reprompt));
    }

    public static SpeechletResponseEnvelope getResponseWithPlainOutputSpeech() {
        return getResponseWithPlainOutputSpeech(DEFAULT_TEXT);
    }

    public static SpeechletResponseEnvelope getResponseWithPlainOutputSpeech(final String text) {
        return getResponseWithPlainOutputSpeech(text, false);
    }

    public static SpeechletResponseEnvelope getResponseWithPlainOutputSpeech(final String text, final boolean tell) {
        final PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(text);
        final Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);
        return envelope(tell ? SpeechletResponse.newTellResponse(outputSpeech) : SpeechletResponse.newAskResponse(outputSpeech, reprompt));
    }

    public static SpeechletResponseEnvelope getResponseWithSimpleCard() {
        return getResponseWithSimpleCard(DEFAULT_CARD_TITLE, DEFAULT_CARD_CONTENT);
    }

    public static SpeechletResponseEnvelope getResponseWithSimpleCard(final String title, final String content) {
        final SimpleCard card = new SimpleCard();
        card.setContent(content);
        card.setTitle(title);
        final SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();
        response.getResponse().setCard(card);
        return response;
    }

    public static SpeechletResponseEnvelope getResponseWithLinkAccountCard() {
        return getResponseWithLinkAccountCard(DEFAULT_CARD_TITLE);
    }

    public static SpeechletResponseEnvelope getResponseWithLinkAccountCard(final String title) {
        final LinkAccountCard card = new LinkAccountCard();
        card.setTitle(title);
        final SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();
        response.getResponse().setCard(card);
        return response;
    }

    public static SpeechletResponseEnvelope getResponseWithStandardCard() {
        return getResponseWithStandardCard(DEFAULT_CARD_TITLE, DEFAULT_CARD_TEXT);
    }

    public static SpeechletResponseEnvelope getResponseWithStandardCard(final String title, final String text) {
        final Image image = new Image();
        image.setSmallImageUrl(DEFAULT_CARD_SMALL_IMAGE);
        image.setLargeImageUrl(DEFAULT_CARD_LARGE_IMAGE);
        return getResponseWithStandardCard(title, text, image);
    }

    public static SpeechletResponseEnvelope getResponseWithStandardCard(final String title, final String text, final Image image) {
        final StandardCard card = new StandardCard();
        card.setText(text);
        card.setTitle(title);
        card.setImage(image);
        final SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();
        response.getResponse().setCard(card);
        return response;
    }

    private static SpeechletResponseEnvelope envelope(final SpeechletResponse response) {
        final SpeechletResponseEnvelope envelope = new SpeechletResponseEnvelope();
        envelope.setResponse(response);
        envelope.setVersion(DEFAULT_VERSION);
        return envelope;
    }
}
