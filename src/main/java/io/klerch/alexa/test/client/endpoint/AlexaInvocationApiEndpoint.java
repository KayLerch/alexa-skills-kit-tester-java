package io.klerch.alexa.test.client.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;

import java.util.HashMap;

public class AlexaInvocationApiEndpoint extends AlexaApiEndpoint {
    @JsonIgnore
    private static final String DEFAULT_ENDPOINT_REGION = "NA";
    @JsonProperty
    private final SkillRequest skillRequest = new SkillRequest();
    @JsonProperty
    final String endpointRegion;

    AlexaInvocationApiEndpoint(final AlexaInvocationApiEndpointBuilder builder) {
        super(builder);
        this.endpointRegion = builder.endpointRegion;
    }

    private class SkillRequest {
        @JsonProperty
        private final String body = "13a35817-70d3-4564-90c1-dee840cb0627";
    }

    public String getService() {
        return "invocations";
    }

    public static AlexaInvocationApiEndpointBuilder create(final HashMap<Object, Object> endpointConfiguration) {
        return new AlexaInvocationApiEndpointBuilder(endpointConfiguration);
    }

    public static AlexaInvocationApiEndpointBuilder create(final String skillId) {
        return new AlexaInvocationApiEndpointBuilder(skillId);
    }

    @Override
    HttpResponse fire(final String payload) {
        try {
            // inject payload
            final String requestPayload = om.writeValueAsString(this).replace("\"" + skillRequest.body + "\"", payload);
            return super.fire(requestPayload);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Could not build request for Invocation API.", e);
        }
    }

    public static class AlexaInvocationApiEndpointBuilder extends AlexaApiEndpointBuilder<AlexaInvocationApiEndpoint> {
        String endpointRegion;

        AlexaInvocationApiEndpointBuilder(final String skillId) {
            super(skillId);
        }

        AlexaInvocationApiEndpointBuilder(final HashMap<Object, Object> endpointConfiguration) {
            super(endpointConfiguration);
            this.endpointRegion = endpointConfiguration.getOrDefault("region", DEFAULT_ENDPOINT_REGION).toString();
        }

        public AlexaApiEndpointBuilder withEndpointRegion(final String endpointRegion) {
            this.endpointRegion = endpointRegion;
            return this;
        }

        @Override
        public AlexaInvocationApiEndpoint build() {
            preBuild();

            Validate.notBlank(endpointRegion, "Endpoint region in your Configuration section must not be empty.");

            return new AlexaInvocationApiEndpoint(this);
        }
    }
}
