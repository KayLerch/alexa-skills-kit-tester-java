package io.klerch.alexa.test.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import io.klerch.alexa.test.AssetFactory;
import org.junit.Assert;
import org.junit.Test;

public class AlexaAssertionTest {
    @Test
    public void sessionEnded() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSsmlOutputSpeech("test", true);
        Assert.assertTrue(AlexaAssertion.SessionEnded.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithSsmlOutputSpeech("test", false);
        Assert.assertFalse(AlexaAssertion.SessionEnded.isTrue(envelope2));
    }

    @Test
    public void sessionStillOpen() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSsmlOutputSpeech("test", false);
        Assert.assertTrue(AlexaAssertion.SessionStillOpen.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithSsmlOutputSpeech("test", true);
        Assert.assertFalse(AlexaAssertion.SessionStillOpen.isTrue(envelope2));
    }

    @Test
    public void sessionHasCard() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSimpleCard();
        Assert.assertTrue(AlexaAssertion.HasCard.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithStandardCard();
        Assert.assertTrue(AlexaAssertion.HasCard.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithLinkAccountCard();
        Assert.assertTrue(AlexaAssertion.HasCard.isTrue(envelope3));
    }

    @Test
    public void sessionHasCardIsSimple() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSimpleCard();
        Assert.assertTrue(AlexaAssertion.HasCardIsSimple.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithStandardCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsSimple.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithLinkAccountCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsSimple.isTrue(envelope3));
    }

    @Test
    public void sessionHasCardIsStandard() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStandardCard();
        Assert.assertTrue(AlexaAssertion.HasCardIsStandard.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithSimpleCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsStandard.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithLinkAccountCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsStandard.isTrue(envelope3));
    }

    @Test
    public void sessionHasCardIsLinkAccount() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithLinkAccountCard();
        Assert.assertTrue(AlexaAssertion.HasCardIsLinkAccount.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithSimpleCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsLinkAccount.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithStandardCard();
        Assert.assertFalse(AlexaAssertion.HasCardIsLinkAccount.isTrue(envelope3));
    }

    @Test
    public void repromptSpeech() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSsmlOutputSpeech();
        Assert.assertTrue(AlexaAssertion.HasRepromptSpeech.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasRepromptSpeechIsSsml.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasRepromptSpeechIsPlainText.isTrue(envelope));

        envelope.getResponse().setReprompt(null);
        Assert.assertFalse(AlexaAssertion.HasRepromptSpeech.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasRepromptSpeechIsSsml.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasRepromptSpeechIsPlainText.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithPlainOutputSpeech();
        Assert.assertTrue(AlexaAssertion.HasRepromptSpeech.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasRepromptSpeechIsPlainText.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasRepromptSpeechIsSsml.isTrue(envelope2));
    }

    @Test
    public void outputSpeech() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSsmlOutputSpeech();
        Assert.assertTrue(AlexaAssertion.HasOutputSpeech.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasOutputSpeechIsSsml.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasOutputSpeechIsPlainText.isTrue(envelope));

        envelope.getResponse().setOutputSpeech(null);
        Assert.assertFalse(AlexaAssertion.HasOutputSpeech.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasOutputSpeechIsSsml.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasOutputSpeechIsPlainText.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithPlainOutputSpeech();
        Assert.assertTrue(AlexaAssertion.HasOutputSpeech.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasOutputSpeechIsPlainText.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasOutputSpeechIsSsml.isTrue(envelope2));
    }

    @Test
    public void directivePlay() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithPlayDirective(PlayBehavior.REPLACE_ALL);
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsStop.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueue.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearEnqueued.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithPlayDirective(PlayBehavior.REPLACE_ENQUEUED);
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithPlayDirective(PlayBehavior.ENQUEUE);
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope3));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope3));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope3));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope3));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope3));
    }

    @Test
    public void directiveStop() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithStopDirective();
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsStop.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueue.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearEnqueued.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithPlayDirective(PlayBehavior.REPLACE_ENQUEUED);
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsStop.isTrue(envelope2));
    }

    @Test
    public void directiveClearQueue() throws Exception {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithClearQueueDirective(ClearBehavior.CLEAR_ALL);
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsClearQueue.isTrue(envelope));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsStop.isTrue(envelope));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearEnqueued.isTrue(envelope));

        final SpeechletResponseEnvelope envelope2 = AssetFactory.givenResponseWithClearQueueDirective(ClearBehavior.CLEAR_ENQUEUED);
        Assert.assertTrue(AlexaAssertion.HasDirective.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsClearQueue.isTrue(envelope2));
        Assert.assertTrue(AlexaAssertion.HasDirectiveIsClearQueueWithClearEnqueued.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlay.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceAll.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithEnqueue.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsPlayWithReplaceEnqueued.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsStop.isTrue(envelope2));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll.isTrue(envelope2));

        final SpeechletResponseEnvelope envelope3 = AssetFactory.givenResponseWithSsmlOutputSpeech();
        Assert.assertFalse(AlexaAssertion.HasDirective.isTrue(envelope3));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueue.isTrue(envelope3));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearEnqueued.isTrue(envelope3));
        Assert.assertFalse(AlexaAssertion.HasDirectiveIsClearQueueWithClearAll.isTrue(envelope3));
    }
}