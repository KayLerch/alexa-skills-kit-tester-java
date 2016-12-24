package io.klerch.alexa.tester;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioItem;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.Stream;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.ClearQueueDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.StopDirective;
import com.amazon.speech.ui.*;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.util.Arrays;
import java.util.Collections;

public class AssetFactory {
    public static final String DEFAULT_AP_PREVIOUS_TOKEN = "previous Token";
    public static final String DEFAULT_AP_URL = "https://stream/stream.mp3";
    public static final String DEFAULT_AP_TOKEN = "token";
    public static final long DEFAULT_AP_OFFSET = 1000L;

    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String DEFAULT_TEXT = "test";
    public static final String DEFAULT_CARD_TITLE = "card_title";
    public static final String DEFAULT_CARD_CONTENT = "card_content";
    public static final String DEFAULT_CARD_TEXT = "card_text";
    public static final String DEFAULT_CARD_SMALL_IMAGE = "https://img/small.jpg";
    public static final String DEFAULT_CARD_LARGE_IMAGE = "https://img/large.jpg";

    public static RequestStreamHandler getRequestStreamHandler() {
        return (inputStream, outputStream, context) -> {
        };
    }

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

    public static SpeechletResponseEnvelope getResponseWithPlayDirective(final PlayBehavior behavior) {
        return getResponseWithPlayDirective(behavior, true);
    }

    public static SpeechletResponseEnvelope getResponseWithPlayDirective(final PlayBehavior behavior, final boolean withAudioItem) {
        SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();

        final PlayDirective playDirective = new PlayDirective();
        playDirective.setPlayBehavior(behavior);

        if (withAudioItem) {
            final Stream stream = new Stream();
            stream.setExpectedPreviousToken(DEFAULT_AP_PREVIOUS_TOKEN);
            stream.setOffsetInMilliseconds(DEFAULT_AP_OFFSET);
            stream.setToken(DEFAULT_AP_TOKEN);
            stream.setUrl(DEFAULT_AP_URL);
            final AudioItem audioItem = new AudioItem();
            audioItem.setStream(stream);
            playDirective.setAudioItem(audioItem);
        }

        response.getResponse().setDirectives(Collections.singletonList(playDirective));
        return response;
    }

    public static SpeechletResponseEnvelope getResponseWithStopDirective() {
        SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();
        response.getResponse().setDirectives(Collections.singletonList(new StopDirective()));
        return response;
    }

    public static SpeechletResponseEnvelope getResponseWithClearQueueDirective(final ClearBehavior behavior) {
        SpeechletResponseEnvelope response = getResponseWithSsmlOutputSpeech();
        ClearQueueDirective clearQueueDirective = new ClearQueueDirective();
        clearQueueDirective.setClearBehavior(behavior);
        response.getResponse().setDirectives(Collections.singletonList(clearQueueDirective));
        return response;
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
