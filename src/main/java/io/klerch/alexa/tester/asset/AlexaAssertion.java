package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.ClearQueueDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.StopDirective;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.LinkAccountCard;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.StandardCard;
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
    HasSimpleCard {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, SimpleCard.class);
        }
    },
    HasStandardCard {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, StandardCard.class);
        }
    },
    HasLinkAccountCard {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return cardInstanceOf(response, LinkAccountCard.class);
        }
    },
    HasReprompt {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getReprompt() != null && response.getResponse().getReprompt().getOutputSpeech() != null;
        }
    },
    HasOutputSpeech {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getOutputSpeech() != null;
        }
    },
    HasDirective {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return response.getResponse().getDirectives() != null && !response.getResponse().getDirectives().isEmpty();
        }
    },
    HasPlayDirective {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, PlayDirective.class);         }
    },
    HasPlayDirectiveToReplaceAll {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return AlexaAssertion.playBehaviorEquals(response, PlayBehavior.REPLACE_ALL);
        }
    },
    HasPlayDirectiveToEnqueue {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return AlexaAssertion.playBehaviorEquals(response, PlayBehavior.ENQUEUE);
        }
    },
    HasPlayDirectiveToReplaceEnqueued {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return playBehaviorEquals(response, PlayBehavior.REPLACE_ENQUEUED);
        }
    },
    HasStopDirective {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, StopDirective.class);        }
    },
    HasClearQueueDirective {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return directiveInstanceOf(response, ClearQueueDirective.class);
        }
    },
    HasClearQueueDirectiveToClearAll {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return clearBehaviorEquals(response, ClearBehavior.CLEAR_ALL);
        }
    },
    HasClearQueueDirectiveToClearEnqueued {
        @Override
        public boolean isTrue(final SpeechletResponseEnvelope response) {
            return clearBehaviorEquals(response, ClearBehavior.CLEAR_ENQUEUED);
        }
    };

    private static boolean playBehaviorEquals(final SpeechletResponseEnvelope response, final PlayBehavior behavior) {
        return HasPlayDirective.isTrue(response) && response.getResponse().getDirectives().stream()
                .filter(PlayDirective.class::isInstance)
                .map(directive -> (PlayDirective)directive)
                .anyMatch(directive -> behavior.equals(directive.getPlayBehavior()));
    }

    private static boolean clearBehaviorEquals(final SpeechletResponseEnvelope response, final ClearBehavior behavior) {
        return HasPlayDirective.isTrue(response) && response.getResponse().getDirectives().stream()
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
}
