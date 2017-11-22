package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.json.SpeechletRequestModule;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioPlayerInterface;
import com.amazon.speech.speechlet.interfaces.display.DisplayInterface;
import com.amazonaws.util.StringUtils;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.klerch.alexa.test.client.endpoint.AlexaEndpoint;
import io.klerch.alexa.test.client.endpoint.AlexaEndpointFactory;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.klerch.alexa.test.client.AlexaClient.API_ENDPOINT.EU;
import static io.klerch.alexa.test.client.AlexaClient.API_ENDPOINT.NA;


public class AlexaClient {
    private final static Logger log = Logger.getLogger(AlexaClient.class);
    private final static ObjectMapper mapper = new ObjectMapper();
    public static final String VERSION = "1.0";
    final AlexaEndpoint endpoint;
    private AlexaResponse lastResponse;
    final String apiEndpoint;
    final long millisFromCurrentDate;
    long lastExecutionTimeMillis;
    final Locale locale;
    final Device device;
    final Application application;
    final User user;
    final Optional<String> debugFlagSessionAttributeName;
    private final Object yLaunch;

    private static Map<API_ENDPOINT, String> apiEndpoints = new HashMap<>();

    static {
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        mapper.registerModule(new SpeechletRequestModule());

        apiEndpoints.putIfAbsent(NA, "https://api.amazonalexa.com/");
        apiEndpoints.putIfAbsent(EU, "https://api.eu.amazonalexa.com/");
    }

    public enum API_ENDPOINT {
        NA, EU
    }

    AlexaClient(final AlexaClientBuilder builder) {
        this.millisFromCurrentDate = builder.timestamp.getTime() - new Date().getTime();
        this.locale = builder.locale;
        apiEndpoint = apiEndpoints.getOrDefault(builder.apiEndpoint, apiEndpoints.get(NA));
        this.application = new Application(builder.applicationId);
        this.user = User.builder().withUserId(builder.uid).withAccessToken(builder.accessToken).build();
        this.device = builder.device;
        this.debugFlagSessionAttributeName = StringUtils.isNullOrEmpty(builder.debugFlagSessionAttributeName) ? Optional.empty() : Optional.of(builder.debugFlagSessionAttributeName);
        this.endpoint = builder.endpoint;
        this.yLaunch = builder.yLaunch;
    }

    public AlexaResponse getLastResponse() {
        return this.lastResponse;
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
        final SpeechletRequestEnvelope envelope = request.getSession().envelope(request);
        String payload = null;
        try {
            payload = mapper.writeValueAsString(envelope);
        } catch (final JsonProcessingException e) {
            final String msg = String.format("Invalid request format. %s", e.getMessage());
            log.error(String.format("→ [ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        return fire(request, payload);
    }

    Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        // ensure payload set
        Validate.notBlank(payload, "Invalid speechlet request contents. Must not be null or empty.");
        // delegate execution to child implementation
        final long startTimestamp = System.currentTimeMillis();
        final Optional<AlexaResponse> response = endpoint.fire(request, payload);
        lastExecutionTimeMillis = System.currentTimeMillis() - startTimestamp;
        response.ifPresent(r -> {
            request.getSession().exploitResponse(r);
            lastResponse = r;
        });
        return response;
    }

    public static AlexaClientBuilder create(final AlexaEndpoint endpoint) {
        return new AlexaClientBuilder(endpoint);
    }

    public static AlexaClientBuilder create(final AlexaEndpoint endpoint, final String applicationId) {
        return create(endpoint).withApplicationId(applicationId);
    }

    public static AlexaClientBuilder create(final InputStream scriptInputStream) throws IOException {
        final YamlReader yamlReader = new YamlReader(IOUtils.toString(scriptInputStream));
        return new AlexaClientBuilder(yamlReader);
    }

    public static AlexaClientBuilder create(final String resourceFilePath) throws IOException {
        final InputStream stream = AlexaClient.class.getClassLoader().getResourceAsStream(resourceFilePath);
        return create(stream);
    }

    public AlexaSession startSession() {
        return new AlexaSession(this);
    }

    /**
     * Starts the script that was loaded from an XML file provided when AlexaClient was created.
     * If you created this client without giving it an XML script-file startScript does
     * nothing as there's no script to read from. In this case use startSession to
     */
    public void startScript() {
        Validate.notNull(yLaunch, "Could not find Launch node. Add this node to the top level of your YAML script and use it as an entry point for your conversation path.");
        startSession().executeSession(yLaunch);
    }

    public Locale getLocale() {
        return locale;
    }

    public static class AlexaClientBuilder {
        AlexaEndpoint endpoint;
        Object yLaunch;
        String applicationId;
        AlexaClient.API_ENDPOINT apiEndpoint;
        Locale locale;
        private String uid;
        private String accessToken;
        String debugFlagSessionAttributeName;
        String deviceId;
        Device device;
        List<Interface> interfaces = new ArrayList<>();
        Date timestamp;

        AlexaClientBuilder(final AlexaEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        AlexaClientBuilder(final YamlReader root) {
            HashMap<Object, Object> yRoot = null;
            try {
                yRoot = (HashMap)root.read();
            } catch (YamlException e) {
                log.error("Could not read YAML script file", e);
            }

            final HashMap yConfig = Optional.ofNullable(yRoot.get("configuration")).filter(o -> o instanceof HashMap).map(o -> (HashMap)o).orElseThrow(() -> new RuntimeException("configuration node is missing or empty."));
            final HashMap yEndpoint = Optional.ofNullable(yConfig.get("endpoint")).filter(o -> o instanceof HashMap).map(o -> (HashMap)o).orElseThrow(() -> new RuntimeException("endpoint node is missing or empty."));

            this.endpoint = AlexaEndpointFactory.createEndpoint(yEndpoint);
            this.applicationId = Optional.ofNullable(yEndpoint.get("skillId")).filter(o -> o instanceof String).map(Object::toString).orElse(System.getenv("skillId"));
            this.locale = Locale.forLanguageTag(Optional.ofNullable(yEndpoint.get("locale")).filter(o -> o instanceof String).map(Object::toString).orElse("en-US"));
            this.apiEndpoint = Optional.ofNullable(yEndpoint.get("region")).filter(o -> o instanceof String).map(o -> AlexaClient.API_ENDPOINT.valueOf(o.toString())).orElse(AlexaClient.API_ENDPOINT.NA);
            this.debugFlagSessionAttributeName = Optional.ofNullable(yConfig.get("debugFlagSessionAttributeName")).filter(o -> o instanceof String).map(Object::toString).orElse(null);

            Optional.ofNullable(yConfig.get("device")).filter(o -> o instanceof HashMap).map(o -> (HashMap)o).ifPresent(yDevice -> {
                this.deviceId = Optional.ofNullable(yDevice.get("id")).map(Object::toString).orElse(System.getenv("skillDeviceId"));

                Optional.ofNullable(yDevice.get("supportedInterfaces")).filter(o -> o instanceof ArrayList).map(o -> (ArrayList)o).ifPresent(yInterfaces -> {
                    yInterfaces.forEach(yInterface -> {
                        final String interfaceName = yInterface.toString();
                        if ("Display".equals(interfaceName)) {
                            withSupportedInterface(DisplayInterface.builder().build());
                        }
                        if ("AudioPlayer".equals(interfaceName)) {
                            withSupportedInterface(AudioPlayerInterface.builder().build());
                        }
                    });
                });
            });

            Optional.ofNullable(yConfig.get("user")).filter(o -> o instanceof HashMap).map(o -> (HashMap)o).ifPresent(yUser -> {
                this.uid = Optional.ofNullable(yUser.get("id")).map(Object::toString).orElse(System.getenv("skillUserId"));
                this.accessToken = Optional.ofNullable(yUser.get("accessToken")).map(Object::toString).orElse(System.getenv("skillAccessToken"));
            });

            yLaunch = Optional.ofNullable(yRoot.get("Launch")).orElseThrow(() -> new RuntimeException("There's no 'Launch'-node provided in the YAML script. Create a top-level node named 'Launch' as it is the entry point for the conversation you'd like to simulate."));
        }

        public AlexaClientBuilder withEndpoint(final AlexaEndpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public AlexaClientBuilder withApiEndpoint(final AlexaClient.API_ENDPOINT apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
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

        public AlexaClientBuilder withDeviceId(final String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public AlexaClientBuilder withDeviceIdRandomized() {
            this.deviceId = UUID.randomUUID().toString();
            return this;
        }

        public AlexaClientBuilder withSupportedInterface(final Interface supportedInterface) {
            if (!interfaces.contains(supportedInterface)) {
                interfaces.add(supportedInterface);
            }
            return this;
        }

        public AlexaClientBuilder withDevice(final Device device) {
            this.device = device;
            return this;
        }

        public AlexaClientBuilder withLocale(final String languageTag) {
            if (!StringUtils.isNullOrEmpty(languageTag)) {
                this.locale = Locale.forLanguageTag(languageTag);
            }
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

        void preBuild() {
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

            if (device == null) {
                SupportedInterfaces supportedInterfaces = null;
                if (!interfaces.isEmpty()) {
                    final SupportedInterfaces.Builder supportedInterfacesBuilder = SupportedInterfaces.builder();
                    interfaces.forEach(supportedInterfacesBuilder::addSupportedInterface);
                    supportedInterfaces = supportedInterfacesBuilder.build();
                } else {
                    supportedInterfaces = SupportedInterfaces.builder().build();
                }
                device = Device.builder().withSupportedInterfaces(supportedInterfaces).withDeviceId(deviceId).build();
            }
        }

        public AlexaClient build() {
            preBuild();
            return new AlexaClient(this);
        }
    }
}
