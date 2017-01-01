package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Application;
import com.amazon.speech.speechlet.User;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.client.endpoint.AlexaEndpoint;
import io.klerch.alexa.test.client.endpoint.AlexaEndpointFactory;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.joox.JOOX.$;

public class AlexaClient {
    private final static Logger log = Logger.getLogger(AlexaClient.class);
    public static final String VERSION = "1.0";
    final AlexaEndpoint endpoint;
    final long millisFromCurrentDate;
    long lastExecutionTimeMillis;
    final Locale locale;
    final Application application;
    final User user;
    final Optional<String> debugFlagSessionAttributeName;
    private final Match mSessions;

    AlexaClient(final AlexaClientBuilder builder) {
        this.millisFromCurrentDate = builder.timestamp.getTime() - new Date().getTime();
        this.locale = builder.locale;
        this.application = new Application(builder.applicationId);
        this.user = User.builder().withUserId(builder.uid).withAccessToken(builder.accessToken).build();
        this.debugFlagSessionAttributeName = StringUtils.isNullOrEmpty(builder.debugFlagSessionAttributeName) ? Optional.empty() : Optional.of(builder.debugFlagSessionAttributeName);
        this.endpoint = builder.endpoint;
        this.mSessions = builder.mSessions;
    }

    public long getLastExecutionMillis() {
        return lastExecutionTimeMillis;
    }

    public AlexaEndpoint getEndpoint() {
        return this.endpoint;
    }

    public static String generateUserId() {
        return String.format("amzn1.ask.account.%s", RandomStringUtils.randomAlphanumeric(207).toUpperCase());
    }

    public static String generateApplicationId() {
        return String.format("amzn1.ask.skill.%s", UUID.randomUUID());
    }

    public Date getCurrentTimestamp() {
        return new Date(new Date().getTime() + millisFromCurrentDate);
    }

    public Application getApplication() { return this.application; }

    public User getUser() { return this.user; }

    public Optional<String> getDebugFlagSessionAttributeName() {
        return debugFlagSessionAttributeName;
    }

    Optional<AlexaResponse> fire(final AlexaRequest request) {
        final SpeechletRequestEnvelope envelope = request.getActor().envelope(request);
        String payload = null;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            payload = mapper.writeValueAsString(envelope);
        } catch (final JsonProcessingException e) {
            final String msg = String.format("Invalid request format. %s", e.getMessage());
            log.error(String.format("→ [ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        // ensure payload set
        Validate.notBlank(payload, "Invalid speechlet request contents. Must not be null or empty.");
        // delegate execution to child implementation
        final long startTimestamp = System.currentTimeMillis();
        final Optional<AlexaResponse> response = endpoint.fire(request, payload);
        lastExecutionTimeMillis = System.currentTimeMillis() - startTimestamp;
        response.ifPresent(request.getActor()::exploitResponse);
        return response;
    }

    public static AlexaClientBuilder create(final AlexaEndpoint endpoint) {
        return new AlexaClientBuilder(endpoint);
    }

    public static AlexaClientBuilder create(final AlexaEndpoint endpoint, final String applicationId) {
        return create(endpoint).withApplicationId(applicationId);
    }

    public static AlexaClientBuilder create(final URI scriptFileUri) throws IOException, SAXException {
        return create($(scriptFileUri).document());
    }

    public static AlexaClientBuilder create(final URL scriptFileUrl) throws IOException, SAXException {
        return create($(scriptFileUrl).document());
    }

    public static AlexaClientBuilder create(final InputStream scriptInputStream) throws IOException, SAXException {
        return create($(scriptInputStream).document());
    }

    public static AlexaClientBuilder create(final File scriptFile) throws IOException, SAXException {
        return create($(scriptFile).document());
    }

    private static AlexaClientBuilder create(final Document document) {
        return new AlexaClientBuilder(document);
    }

    public AlexaSessionActor startSession() {
        return new AlexaSessionActor(this);
    }

    /**
     * Starts the script that was loaded from an XML file provided when AlexaClient was created.
     * If you created this client without giving it an XML script-file startScript does
     * nothing as there's no script to read from. In this case use startSession to
     */
    public void startScript() {
        if (mSessions.isEmpty()) {
            log.warn("No test-sessions defined in script-file. Nothing is executed in this test-run.");
            return;
        }

        mSessions.children().forEach(session -> {
            final AlexaSessionActor actor = this.startSession();
            actor.executeSession(session);
            actor.endSession();
        });
    }

    public Locale getLocale() {
        return locale;
    }

    public static class AlexaClientBuilder {
        AlexaEndpoint endpoint;
        Match mSessions;
        String applicationId;
        Locale locale;
        String uid;
        String accessToken;
        String debugFlagSessionAttributeName;
        Date timestamp;

        AlexaClientBuilder(final AlexaEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        AlexaClientBuilder(final Document document) {
            // root
            final Match mTest = $(document).first();
            Validate.isTrue(mTest.isNotEmpty(), "Root node 'test' not found.");

            // configuration
            final Match mConfig = mTest.find("configuration");
            Validate.isTrue(mConfig.isNotEmpty(), "Node 'configuration' not found.");

            final Match mEndpoint = mConfig.find("endpoint");
            Validate.isTrue(mEndpoint.isNotEmpty(), "Node 'endpoint' not found.");

            this.endpoint = AlexaEndpointFactory.createEndpoint(mEndpoint);

            mSessions = mTest.find("sessions");

            final Match mApplication = mConfig.find("application");
            if (mApplication.isNotEmpty()) {
                withApplicationId(mApplication.id());
            }

            final Match mLocale = mConfig.find("locale");
            if (mLocale.isNotEmpty()) {
                withLocale(mLocale.text());
            }

            final Match mDebugFlag = mConfig.find("debugFlagSessionAttribute");
            if (mDebugFlag.isNotEmpty()) {
                withDebugFlagSessionAttribute(mDebugFlag.text());
            }

            final Match mTimestamp = mConfig.find("timestamp");
            if (mTimestamp.isNotEmpty()) {
                withTimestamp(new Date(mTimestamp.text(Timestamp.class).getTime()));
            }

            final Match mUser = mConfig.find("user");
            if (mUser.isNotEmpty()) {
                withUserId(mUser.id());
                withAccessToken(mUser.attr("accessToken"));
            }
        }

        public AlexaClientBuilder withEndpoint(final AlexaEndpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public AlexaClientBuilder withApplicationId(final String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public AlexaClientBuilder withLocale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        public AlexaClientBuilder withLocale(final String languageTag) {
            this.locale = Locale.forLanguageTag(languageTag);
            return this;
        }

        public AlexaClientBuilder withUserId(final String uid) {
            this.uid = uid;
            return this;
        }

        public AlexaClientBuilder withAccessToken(final String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AlexaClientBuilder withTimestamp(final Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AlexaClientBuilder withDebugFlagSessionAttribute(final String debugFlagSessionAttributeName) {
            this.debugFlagSessionAttributeName = debugFlagSessionAttributeName;
            return this;
        }

        public AlexaClient build() {
            Validate.notNull(endpoint, "Endpoint must not be null.");

            if (StringUtils.isNullOrEmpty(applicationId)) {
                applicationId = generateApplicationId();
            }

            if (locale == null) {
                locale = Locale.US;
            }
            if (StringUtils.isNullOrEmpty(uid)) {
                uid = generateUserId();
            }
            if (timestamp == null) {
                timestamp = new Date();
            }

            return new AlexaClient(this);
        }
    }
}
