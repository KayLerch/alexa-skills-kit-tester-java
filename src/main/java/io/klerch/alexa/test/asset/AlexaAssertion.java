package io.klerch.alexa.test.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.ClearQueueDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.StopDirective;
import com.amazon.speech.ui.*;
import org.apache.commons.lang3.BooleanUtils;

public enum AlexaAssertion implements AlexaAssertionValidator {
    SessionEnded {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return BooleanUtils.isTrue(response.getResponse().getShouldEndSession());
        }
    },
    SessionStillOpen {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return BooleanUtils.isFalse(response.getResponse().getShouldEndSession());
        }
    },
    HasCard {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getCard() != null;
        }
    },
    HasCardIsSimple {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, SimpleCard.class);
        }
    },
    HasCardIsStandard {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, StandardCard.class);
        }
    },
    HasCardIsLinkAccount {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, LinkAccountCard.class);
        }
    },
    HasRepromptSpeech {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getReprompt() != null && response.getResponse().getReprompt().getOutputSpeech() != null;
        }
    },
    HasRepromptSpeechIsSsml {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return repromptOutputSpeechInstanceOf(response, SsmlOutputSpeech.class);
        }
    },
    HasRepromptSpeechIsPlainText {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return repromptOutputSpeechInstanceOf(response, PlainTextOutputSpeech.class);
        }
    },
    HasOutputSpeech {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getOutputSpeech() != null;
        }
    },
    HasOutputSpeechIsSsml {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return outputSpeechInstanceOf(response, SsmlOutputSpeech.class);
        }
    },
    HasOutputSpeechIsPlainText {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return outputSpeechInstanceOf(response, PlainTextOutputSpeech.class);
        }
    },
    HasDirective {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getDirectives() != null && !response.getResponse().getDirectives().isEmpty();
        }
    },
    HasDirectiveIsPlay {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, PlayDirective.class);         }
    },
    HasDirectiveIsPlayWithReplaceAll {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return AlexaAssertion.playBehaviorEquals(response, PlayBehavior.REPLACE_ALL);
        }
    },
    HasDirectiveIsPlayWithEnqueue {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return AlexaAssertion.playBehaviorEquals(response, PlayBehavior.ENQUEUE);
        }
    },
    HasDirectiveIsPlayWithReplaceEnqueued {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return playBehaviorEquals(response, PlayBehavior.REPLACE_ENQUEUED);
        }
    },
    HasDirectiveIsStop {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, StopDirective.class);        }
    },
    HasDirectiveIsClearQueue {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, ClearQueueDirective.class);
        }
    },
    HasDirectiveIsClearQueueWithClearAll {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return clearBehaviorEquals(response, ClearBehavior.CLEAR_ALL);
        }
    },
    HasDirectiveIsClearQueueWithClearEnqueued {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return clearBehaviorEquals(response, ClearBehavior.CLEAR_ENQUEUED);
        }
    };

    private static boolean playBehaviorEquals(final SpeechletResponseEnvelope response, final PlayBehavior behavior) {
        return HasDirectiveIsPlay.isTrue(response) && response.getResponse().getDirectives().stream()
                .filter(PlayDirective.class::isInstance)
                .map(directive -> (PlayDirective)directive)
                .anyMatch(directive -> behavior.equals(directive.getPlayBehavior()));
    }

    private static boolean clearBehaviorEquals(final SpeechletResponseEnvelope response, final ClearBehavior behavior) {
        return HasDirectiveIsClearQueue.isTrue(response) && response.getResponse().getDirectives().stream()
                .filter(ClearQueueDirective.class::isInstance)
                .map(directive -> (ClearQueueDirective)directive)
                .anyMatch(directive -> behavior.equals(directive.getClearBehavior()));
    }

    private static <TDirective extends Directive> boolean directiveInstanceOf(final SpeechletResponseEnvelope response, final Class<TDirective> directiveClass) {
        return HasDirective.isTrue(response) && response.getResponse().getDirectives().stream().anyMatch(directiveClass::isInstance);
    }

    private static <TCard extends Card> boolean cardInstanceOf(final SpeechletResponseEnvelope response, final Class<TCard> cardClass) {
        return HasCard.isTrue(response) && cardClass.isInstance(response.getResponse().getCard());
    }

    private static <TOutputSpeech extends OutputSpeech> boolean outputSpeechInstanceOf(final SpeechletResponseEnvelope response, final Class<TOutputSpeech> outputSpeechClass) {
        return HasOutputSpeech.isTrue(response) && outputSpeechClass.isInstance(response.getResponse().getOutputSpeech());
    }

    private static <TOutputSpeech extends OutputSpeech> boolean repromptOutputSpeechInstanceOf(final SpeechletResponseEnvelope response, final Class<TOutputSpeech> outputSpeechClass) {
        return HasRepromptSpeech.isTrue(response) && outputSpeechClass.isInstance(response.getResponse().getReprompt().getOutputSpeech());
    }
}
