package io.klerch.alexa.tester.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Application;
import com.amazon.speech.speechlet.User;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.actor.AlexaSessionActor;
import io.klerch.alexa.tester.request.AlexaRequest;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.apache.commons.lang3.Validate;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public abstract class AlexaClient {
    public final String VERSION = "1.0.0";
    final Locale locale;
    final Application application;
    final User user;
    final ObjectMapper mapper;

    AlexaClient(final AlexaTestBuilder builder) {
        this.mapper = new ObjectMapper();
        this.locale = builder.locale;
        this.application = new Application(builder.applicationId);
        this.user = User.builder().withUserId(builder.uid).withAccessToken(builder.accessToken).build();
    }

    public Application getApplication() { return this.application; }

    public User getUser() { return this.user; }

    public Optional<AlexaResponse> fire(final AlexaRequest request) {
        final SpeechletRequestEnvelope envelope = request.getActor().envelope(request);
        String payload = null;
        try {
            payload = mapper.writeValueAsString(envelope);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Invalid request format.", e);
        }
        // ensure payload set
        Validate.notBlank(payload, "Invalid speechlet request contents. Must not be null or empty.");
        // delegate execution to child implementation
        final Optional<AlexaResponse> response = fire(request, payload);
        response.ifPresent(request.getActor()::exploitResponse);
        return response;
    }

    abstract Optional<AlexaResponse> fire(final AlexaRequest request, final String payload);

    public AlexaSessionActor startSession() {
        return new AlexaSessionActor(this);
    }

    public Locale getLocale() {
        return locale;
    }

    public static abstract class AlexaTestBuilder<T extends AlexaClient, G extends AlexaTestBuilder> {
        String applicationId;
        Locale locale;
        String uid;
        String accessToken;

        AlexaTestBuilder(final String applicationId) {
            this.applicationId = applicationId;
        }

        public G withLocale(final Locale locale) {
            this.locale = locale;
            return (G)this;
        }

        public G withLocale(final String languageTag) {
            this.locale = Locale.forLanguageTag(languageTag);
            return (G)this;
        }

        public G withUserId(final String uid) {
            this.uid = uid;
            return (G)this;
        }

        public G withAccessToken(final String accessToken) {
            this.accessToken = accessToken;
            return (G)this;
        }

        void preBuild() {
            Validate.notBlank(applicationId, "Application Id must not be empty.");

            if (locale == null) {
                locale = Locale.US;
            }
            if (StringUtils.isNullOrEmpty(uid)) {
                uid = UUID.randomUUID().toString();
            }
        }

        public abstract T build();
    }
}
