package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum AlexaAsset implements AlexaAssetValidator {
    Card {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasCard.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final String json = mapper.writeValueAsString(response.getResponse().getCard());
                return this.exists(response) && AlexaAsset.equals(json, value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final String json = mapper.writeValueAsString(response.getResponse().getCard());
                return this.exists(response) && AlexaAsset.matches(json, pattern);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
    },
    SimpleCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasCardIsSimple.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return this.exists(response) && Card.equals(response, value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return this.exists(response) && Card.matches(response, pattern);
        }
    },
    SimpleCardTitle {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return SimpleCard.exists(response) && AlexaAsset.exists(response.getResponse().getCard().getTitle());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return SimpleCard.exists(response) && AlexaAsset.equals(response.getResponse().getCard().getTitle(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return SimpleCard.exists(response) && AlexaAsset.matches(response.getResponse().getCard().getTitle(), pattern);
        }
    },
    SimpleCardContent {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return SimpleCard.exists(response) && AlexaAsset.exists(((com.amazon.speech.ui.SimpleCard)response.getResponse().getCard()).getContent());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return SimpleCard.exists(response) && AlexaAsset.equals(((com.amazon.speech.ui.SimpleCard)response.getResponse().getCard()).getContent(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return SimpleCard.exists(response) && AlexaAsset.matches(((com.amazon.speech.ui.SimpleCard)response.getResponse().getCard()).getContent(), pattern);
        }
    },

    StandardCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasCardIsStandard.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return this.exists(response) && Card.equals(response, value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return this.exists(response) && Card.matches(response, pattern);
        }
    },
    StandardCardTitle {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return StandardCard.exists(response) && AlexaAsset.exists(response.getResponse().getCard().getTitle());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return StandardCard.exists(response) && AlexaAsset.equals(response.getResponse().getCard().getTitle(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return StandardCard.exists(response) && AlexaAsset.matches(response.getResponse().getCard().getTitle(), pattern);
        }
    },
    StandardCardText {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return StandardCard.exists(response) && AlexaAsset.exists(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getText());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return StandardCard.exists(response) && AlexaAsset.equals(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getText(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return StandardCard.exists(response) && AlexaAsset.matches(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getText(), pattern);
        }
    },
    StandardCardImage {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return StandardCard.exists(response) && AlexaAsset.exists(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return this.exists(response) && AlexaAsset.equals(mapper.writeValueAsString(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage()), value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return this.exists(response) && AlexaAsset.matches(mapper.writeValueAsString(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage()), pattern);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
    },
    StandardCardSmallImageUrl {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return StandardCardImage.exists(response) && AlexaAsset.exists(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getSmallImageUrl());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return StandardCardImage.exists(response) && AlexaAsset.equals(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getSmallImageUrl(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return StandardCardImage.exists(response) && AlexaAsset.matches(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getSmallImageUrl(), pattern);
        }
    },
    StandardCardLargeImageUrl {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return StandardCardImage.exists(response) && AlexaAsset.exists(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getLargeImageUrl());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return StandardCardImage.exists(response) && AlexaAsset.equals(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getLargeImageUrl(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return StandardCardImage.exists(response) && AlexaAsset.matches(((com.amazon.speech.ui.StandardCard)response.getResponse().getCard()).getImage().getLargeImageUrl(), pattern);
        }
    },
    LinkAccountCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasCardIsLinkAccount.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return this.exists(response) && Card.equals(response, value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return this.exists(response) && Card.matches(response, pattern);
        }
    },
    LinkAccountCardTitle {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return LinkAccountCard.exists(response) && AlexaAsset.exists(response.getResponse().getCard().getTitle());
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return LinkAccountCard.exists(response) && AlexaAsset.equals(response.getResponse().getCard().getTitle(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return LinkAccountCard.exists(response) && AlexaAsset.matches(response.getResponse().getCard().getTitle(), pattern);
        }
    },
    OutputSpeech {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasOutputSpeech.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final String json = mapper.writeValueAsString(response.getResponse().getOutputSpeech());
                return this.exists(response) && AlexaAsset.equals(json, value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final String json = mapper.writeValueAsString(response.getResponse().getOutputSpeech());
                return this.exists(response) && AlexaAsset.matches(json, pattern);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
    },
    OutputSpeechSsml {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasOutputSpeechIsSsml.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return exists(response) && AlexaAsset.equals(((SsmlOutputSpeech)response.getResponse().getOutputSpeech()).getSsml(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return exists(response) && AlexaAsset.matches(((SsmlOutputSpeech)response.getResponse().getOutputSpeech()).getSsml(), pattern);
        }
    },
    OutputSpeechPlainText {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasOutputSpeechIsPlainText.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return exists(response) && AlexaAsset.equals(((PlainTextOutputSpeech)response.getResponse().getOutputSpeech()).getText(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return exists(response) && AlexaAsset.matches(((PlainTextOutputSpeech)response.getResponse().getOutputSpeech()).getText(), pattern);
        }
    },
    RepromptSpeech {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasRepromptSpeech.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return this.exists(response) && AlexaAsset.equals(mapper.writeValueAsString(response.getResponse().getReprompt().getOutputSpeech()), value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                return this.exists(response) && AlexaAsset.matches(mapper.writeValueAsString(response.getResponse().getReprompt().getOutputSpeech()), pattern);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return false;
            }
        }
    },
    RepromptSpeechSsml {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasRepromptSpeechIsSsml.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return exists(response) && AlexaAsset.equals(((SsmlOutputSpeech)response.getResponse().getReprompt().getOutputSpeech()).getSsml(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return exists(response) && AlexaAsset.matches(((SsmlOutputSpeech)response.getResponse().getReprompt().getOutputSpeech()).getSsml(), pattern);
        }
    },
    RepromptSpeechPlainText {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return AlexaAssertion.HasRepromptSpeechIsPlainText.isTrue(response);
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return exists(response) && AlexaAsset.equals(((PlainTextOutputSpeech)response.getResponse().getReprompt().getOutputSpeech()).getText(), value);
        }

        @Override
        public boolean matches(SpeechletResponseEnvelope response, String pattern) {
            return exists(response) && AlexaAsset.matches(((PlainTextOutputSpeech)response.getResponse().getReprompt().getOutputSpeech()).getText(), pattern);
        }
    };

    private static boolean exists(final Object o) {
        return o != null;
    }

    private static boolean equals(final Object o, final Object value) {
        return (!exists(o) && value == null) || o.equals(value);
    }

    private static boolean matches(final Object o, final String pattern) {
        return exists(o) && String.valueOf(o).matches(pattern);
    }
}
