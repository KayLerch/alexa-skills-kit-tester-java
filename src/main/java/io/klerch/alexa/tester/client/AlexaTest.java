package io.klerch.alexa.tester.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Application;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.User;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.tester.request.AlexaRequest;
import io.klerch.alexa.tester.request.AlexaSessionStartedRequest;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.apache.commons.lang3.Validate;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public abstract class AlexaTest {
    boolean sessionClosed;
    static final String VERSION = "1.0.0";
    final Locale locale;
    final Session session;
    final ObjectMapper mapper;

    AlexaTest(final AlexaTestBuilder builder) {
        this.sessionClosed = false;
        this.mapper = new ObjectMapper();
        this.locale = builder.locale;
        final Application application = new Application(builder.applicationId);
        final User user = User.builder().withUserId(builder.uid).withAccessToken(builder.accessToken).build();
        this.session = Session.builder()
                .withApplication(application)
                .withUser(user)
                .withIsNew(false)
                .withSessionId(UUID.randomUUID().toString())
                .build();
    }

    private Session getSessionWithIsNew() {
        return Session.builder()
                .withApplication(session.getApplication())
                .withUser(session.getUser())
                .withIsNew(true)
                .withSessionId(session.getSessionId())
                .withAttributes(session.getAttributes())
                .build();
    }

    private void applySessionAttributes(final Map<String, Object> attributes) {
        // reset attributes first
        this.session.getAttributes().clear();
        if (attributes != null) {
            // apply attributes one by one
            attributes.forEach(this.session::setAttribute);
        }
    }

    AlexaResponse fire(final AlexaRequest request) {
        final SpeechletRequestEnvelope envelope = SpeechletRequestEnvelope.builder()
                .withRequest(request.getSpeechletRequest())
                .withSession(request instanceof AlexaSessionStartedRequest ? getSessionWithIsNew() : session)
                .withVersion(VERSION)
                .build();
        String payload = null;
        try {
            payload = mapper.writeValueAsString(envelope);
        } catch (final JsonProcessingException e) {
            payload = null;
            throw new RuntimeException("Invalid response format from Lambda function.", e);
        }
        // ensure payload set
        Validate.notBlank(payload, "Invalid speechlet request contents. Must not be null or empty.");
        // ensure session is ready for another request
        Validate.isTrue(!sessionClosed, "Session already closed and not ready for '%s'", payload);
        // delegate execution to child implementation
        final AlexaResponse response = fire(request, payload);
        if (request.expectsResponse()) {
            // remember session closed
            sessionClosed = response.getShouldEndSession();
            // apply session attributes for next request
            applySessionAttributes(response.getSessionAttributes());
        }
        return response;
    }

    abstract AlexaResponse fire(final AlexaRequest request, final String payload);

    public AlexaTestActor start() {
        return new AlexaTestActor(this);
    }

    public Locale getLocale() {
        return locale;
    }

    public Session getSession() {
        return session;
    }

    public static abstract class AlexaTestBuilder<T extends AlexaTest, G extends AlexaTestBuilder> {
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
