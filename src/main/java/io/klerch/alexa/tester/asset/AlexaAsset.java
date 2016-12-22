package io.klerch.alexa.tester.asset;

import com.amazon.speech.json.SpeechletResponseEnvelope;

public enum AlexaAsset implements AlexaAssetValidator {
    SimpleCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    SimpleCardTitle {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    SimpleCardText {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },

    StandardCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    StandardCardTitle {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    StandardCardContent {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    StandardCardSmallImageUrl {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    StandardCardLargeImageUrl {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },

    AccountLinkingCard {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },

    OutputSpeech {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    OutputSpeechSsml {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    OutputSpeechPlain {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },

    RepromptSpeech {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    RepromptSpeechSsml {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },
    RepromptSpeechPlain {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    },

    SessionEndedFlag {
        @Override
        public boolean exists(SpeechletResponseEnvelope response) {
            return false;
        }

        @Override
        public boolean equals(SpeechletResponseEnvelope response, Object value) {
            return false;
        }
    }
}
