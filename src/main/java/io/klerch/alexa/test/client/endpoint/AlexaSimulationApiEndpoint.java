package io.klerch.alexa.test.client.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.request.AlexaUtteranceRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

public class AlexaSimulationApiEndpoint extends AlexaApiEndpoint {
    @JsonIgnore
    private static final String DEFAULT_LOCALE = Locale.US.toLanguageTag();
    @JsonProperty
    Input input = new Input();
    @JsonProperty
    final Device device = new Device();

    AlexaSimulationApiEndpoint(final AlexaSimulationApiEndpointBuilder builder) {
        super(builder);
        device.locale = builder.locale;
    }

    private class Device {
        @JsonProperty
        private String locale;
    }

    private class Input {
        @JsonProperty
        private String content;
    }

    public String getService() {
        return "simulations";
    }

    public static AlexaSimulationApiEndpointBuilder create(final HashMap<Object, Object> endpointConfiguration) {
        return new AlexaSimulationApiEndpointBuilder(endpointConfiguration);
    }

    public static AlexaSimulationApiEndpointBuilder create(final String skillId) {
        return new AlexaSimulationApiEndpointBuilder(skillId);
    }

    @Override
    public Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        if (!(request instanceof AlexaUtteranceRequest)) {
            log.info("Skip " + request.getClass().getName() + " as it is not supported by Simulation-Api endpoint.");
            return Optional.empty();
        } else {
            return super.fire(request, payload);
        }
    }

    @Override
    HttpResponse fire(final String utterance) {
        try {
            input.content = utterance;
            // inject payload
            final String requestPayload = om.writeValueAsString(this);
            return super.fire(requestPayload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Could not build request for Simulation API. ", e);
        }
    }

    public static class AlexaSimulationApiEndpointBuilder extends AlexaApiEndpointBuilder<AlexaSimulationApiEndpoint> {
        String locale;

        AlexaSimulationApiEndpointBuilder(final String skillId) {
            super(skillId);
        }

        AlexaSimulationApiEndpointBuilder(final HashMap<Object, Object> endpointConfiguration) {
            super(endpointConfiguration);
            this.locale = endpointConfiguration.getOrDefault("locale", DEFAULT_LOCALE).toString();
        }

        public AlexaApiEndpointBuilder withLocale(final String locale) {
            this.locale = locale;
            return this;
        }

        public AlexaApiEndpointBuilder withLocale(final Locale locale) {
            return withLocale(locale.toLanguageTag());
        }

        @Override
        public AlexaSimulationApiEndpoint build() {
            preBuild();

            Validate.notBlank(locale, "Locale must be provided.");

            return new AlexaSimulationApiEndpoint(this);
        }
    }
}
