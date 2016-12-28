package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Application;
import com.amazon.speech.speechlet.User;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.actor.AlexaSessionActor;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public abstract class AlexaClient {
    private final static Logger log = Logger.getLogger(AlexaClient.class);
    public static final String VERSION = "1.0";
    final Locale locale;
    final Application application;
    final User user;
    final ObjectMapper mapper;
    final Optional<String> debugFlagSessionAttributeName;

    AlexaClient(final AlexaClientBuilder builder) {
        this.mapper = new ObjectMapper();
        this.locale = builder.locale;
        this.application = new Application(builder.applicationId);
        this.user = User.builder().withUserId(builder.uid).withAccessToken(builder.accessToken).build();
        this.debugFlagSessionAttributeName = StringUtils.isNullOrEmpty(builder.debugFlagSessionAttributeName) ? Optional.empty() : Optional.of(builder.debugFlagSessionAttributeName);
    }

    public static String generateUserId() {
        return String.format("amzn1.ask.account.%s", RandomStringUtils.randomAlphanumeric(207).toUpperCase());
    }

    public static String generateApplicationId() {
        return String.format("amzn1.ask.skill.%s", UUID.randomUUID());
    }

    public Application getApplication() { return this.application; }

    public User getUser() { return this.user; }

    public Optional<String> getDebugFlagSessionAttributeName() {
        return debugFlagSessionAttributeName;
    }

    public Optional<AlexaResponse> fire(final AlexaRequest request) {
        final SpeechletRequestEnvelope envelope = request.getActor().envelope(request);
        String payload = null;
        try {
            payload = mapper.writeValueAsString(envelope);
        } catch (final JsonProcessingException e) {
            final String msg = String.format("Invalid request format. %s", e.getMessage());
            log.error(String.format("→ [ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        // ensure payload set
        Validate.notBlank(payload, "Invalid speechlet request contents. Must not be null or empty.");
        // delegate execution to child implementation
        final Optional<AlexaResponse> response = fire(request, payload);
        response.ifPresent(request.getActor()::exploitResponse);
        return response;
    }

    public abstract long getLastExecutionMillis();

    abstract Optional<AlexaResponse> fire(final AlexaRequest request, final String payload);

    public AlexaSessionActor startSession() {
        return new AlexaSessionActor(this);
    }

    public Locale getLocale() {
        return locale;
    }

    public static abstract class AlexaClientBuilder<T extends AlexaClient, G extends AlexaClientBuilder> {
        String applicationId;
        Locale locale;
        String uid;
        String accessToken;
        String debugFlagSessionAttributeName;

        AlexaClientBuilder(final String applicationId) {
            this.applicationId = applicationId;
        }

        public AlexaClientBuilder<T, G> withLocale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        public AlexaClientBuilder<T, G> withLocale(final String languageTag) {
            this.locale = Locale.forLanguageTag(languageTag);
            return this;
        }

        public AlexaClientBuilder<T, G> withUserId(final String uid) {
            this.uid = uid;
            return this;
        }

        public AlexaClientBuilder<T, G> withAccessToken(final String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AlexaClientBuilder<T, G> withDebugFlagSessionAttribute(final String debugFlagSessionAttributeName) {
            this.debugFlagSessionAttributeName = debugFlagSessionAttributeName;
            return this;
        }

        void preBuild() {
            Validate.notBlank(applicationId, "Application Id must not be empty.");

            if (locale == null) {
                locale = Locale.US;
            }
            if (StringUtils.isNullOrEmpty(uid)) {
                uid = generateUserId();
            }
        }

        public abstract T build();
    }
}
