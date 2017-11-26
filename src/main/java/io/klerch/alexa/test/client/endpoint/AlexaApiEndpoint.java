package io.klerch.alexa.test.client.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class AlexaApiEndpoint implements AlexaEndpoint {
    @JsonIgnore
    public static final String LWA_CLIENT_ID_PROPERTY = "lwaClientId";
    @JsonIgnore
    public static final String LWA_CLIENT_SECRET_PROPERTY = "lwaClientSecret";
    @JsonIgnore
    public static final String LWA_REFRESH_TOKEN_PROPERTY = "lwaRefreshToken";
    @JsonIgnore
    public static final String LWA_ACCESS_TOKEN_PROPERTY = "lwaAccessToken";
    @JsonIgnore
    private static final String IN_PROGRESS = "IN_PROGRESS";

    @JsonIgnore
    private final String lwaClientId;
    @JsonIgnore
    private final String lwaClientSecret;
    @JsonIgnore
    private final String lwaRefreshToken;
    @JsonIgnore
    private String lwaAccessToken;

    @JsonIgnore
    static final String SKILL_REF_SEPARATOR = "://";
    @JsonIgnore
    String id = "";
    @JsonIgnore
    final static ObjectMapper om = new ObjectMapper();
    @JsonIgnore
    final static Logger log = Logger.getLogger(AlexaInvocationApiEndpoint.class);
    @JsonIgnore
    final String skillId;

    AlexaApiEndpoint(final AlexaApiEndpointBuilder builder) {
        this.skillId = builder.skillId;

        this.lwaClientId = Optional.ofNullable(builder.lwaClientId).filter(StringUtils::isNotBlank).orElse(System.getenv(LWA_CLIENT_ID_PROPERTY));
        Validate.notBlank(this.lwaClientId, "Missing the " + LWA_CLIENT_ID_PROPERTY + " as an environment variable.");

        this.lwaClientSecret = Optional.ofNullable(builder.lwaClientSecret).filter(StringUtils::isNotBlank).orElse(System.getenv(LWA_CLIENT_SECRET_PROPERTY));
        Validate.notBlank(this.lwaClientSecret, "Missing the " + LWA_CLIENT_SECRET_PROPERTY + " as an environment variable.");

        this.lwaRefreshToken = Optional.ofNullable(builder.lwaRefreshToken).filter(StringUtils::isNotBlank).orElse(System.getenv(LWA_REFRESH_TOKEN_PROPERTY));
        Validate.notBlank(this.lwaRefreshToken, "Missing the " + LWA_REFRESH_TOKEN_PROPERTY + " as an environment variable.");

        this.lwaAccessToken = System.getProperty(LWA_ACCESS_TOKEN_PROPERTY);
    }

    @JsonIgnore
    public abstract String getService();

    @Override
    public Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        JsonNode root = null;
        String statusCode = IN_PROGRESS;

        if (StringUtils.isBlank(lwaAccessToken)) {
            refreshToken();
        }

        while (IN_PROGRESS.equals(statusCode)) {
            // call API
            HttpResponse httpResponse = fire(payload);
            // check if returned Unauthorized code
            if (httpResponse.getStatusLine().getStatusCode() == 401) {
                // try refresh token
                refreshToken();
                // call API again
                httpResponse = fire(payload);
            }
            id = "";

            Validate.inclusiveBetween(200, 399, httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());

            final HttpEntity responseEntity = httpResponse.getEntity();
            final String responsePayload;

            try {
                responsePayload = IOUtils.toString(responseEntity.getContent(), "UTF-8");
                log.debug(responsePayload);
                root = om.readTree(responsePayload);
            } catch (final IOException e) {
                throw new RuntimeException("Invalid response from SMAPI. " + e.getMessage());
            }

            statusCode = root.get("status").textValue();

            Validate.isTrue(!StringUtils.equalsIgnoreCase("FAILED", statusCode), "SMAPI returned with an error. " + responsePayload);

            if (IN_PROGRESS.equals(statusCode)) {
                // grab id for next request to poll for completion
                id = root.get("id").textValue();
                log.info("Asynchronous processing in progress. Keep on polling for result of transaction with id " + id);
            }
        }

        final String endpoint = root.get("result").get("skillExecutionInfo").get("invocationRequest").get("endpoint").textValue();
        log.info("Endpoint: " + endpoint);

        final JsonNode responseBody = root.get("result").get("skillExecutionInfo").get("invocationResponse").get("body");

        Validate.notNull(responseBody, "Skill returned an invalid response");

        try {
            return Optional.of(new AlexaResponse(request, payload, om.writeValueAsString(responseBody)));
        } catch (IOException e) {
            throw new RuntimeException("Could not parse skill response received from SMAPI. " + e.getMessage());
        }
    }

    HttpResponse fire(final String payload) {
        try {
            return HttpClientBuilder.create().build().execute(getRequest(payload));
        } catch (final IOException e) {
            throw new RuntimeException("Error received from SMAPI. " + e.getMessage());
        }
    }

    HttpUriRequest getRequest(final String payload) {
        final String url = "https://api.amazonalexa.com/v0/skills/" + this.skillId + "/" + getService() + (StringUtils.isNotBlank(id) ? "/" : "") + id;

        final HttpUriRequest request = StringUtils.isNotBlank(id) ? new HttpGet(url) : new HttpPost(url);
        request.setHeader(HttpHeaders.CONTENT_TYPE,"application/json");
        request.setHeader(HttpHeaders.ACCEPT,"application/json");
        request.setHeader(HttpHeaders.AUTHORIZATION, lwaAccessToken);

        if (request instanceof HttpPost) {
            final HttpEntity requestEntity = new ByteArrayEntity(payload.getBytes());
            ((HttpPost)request).setEntity(requestEntity);
        }
        return request;
    }

    public void refreshToken() {
        final String url = "https://api.amazon.com/auth/o2/token";

        final HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE,"application/x-www-form-urlencoded;charset=UTF-8");

        httpPost.setHeader(HttpHeaders.ACCEPT,"application/json");
        httpPost.setHeader(HttpHeaders.ACCEPT_ENCODING,"application/json");

        final List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", "refresh_token"));
        nameValuePairs.add(new BasicNameValuePair("refresh_token", lwaRefreshToken));
        nameValuePairs.add(new BasicNameValuePair("client_id", lwaClientId));
        nameValuePairs.add(new BasicNameValuePair("client_secret", lwaClientSecret));

        final HttpResponse httpResponse;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httpResponse = HttpClientBuilder.create().build().execute(httpPost);
            Validate.inclusiveBetween(200, 399, httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
            final HttpEntity responseEntity = httpResponse.getEntity();
            final String responsePayload = IOUtils.toString(responseEntity.getContent(), "UTF-8");
            cacheAccessToken(om.readTree(responsePayload));
        } catch (final IOException e) {
            throw new RuntimeException("Error received from Login with Amazon on refreshing an access token. " + e.getMessage(), e);
        }
    }

    private void cacheAccessToken(final JsonNode token) {
        lwaAccessToken = Optional.ofNullable(token.get("access_token")).orElseThrow(() -> new RuntimeException("Could not obtain access token from Amazon Login.")).textValue();
        System.setProperty(LWA_ACCESS_TOKEN_PROPERTY, lwaAccessToken);
    }

    public abstract static class AlexaApiEndpointBuilder<T extends AlexaApiEndpoint> {
        String skillId;
        String lwaClientId;
        String lwaClientSecret;
        String lwaRefreshToken;

        AlexaApiEndpointBuilder(final String skillId) {
            this.skillId = skillId;
        }

        AlexaApiEndpointBuilder(final HashMap<Object, Object> endpointConfiguration) {
            Validate.notEmpty(endpointConfiguration, "configuration section in your YAML file must not be empty. At least a type needs to be set.");
            this.skillId = Optional.ofNullable(endpointConfiguration.get("skillId"))
                    .filter(o -> o instanceof String)
                    .map(Object::toString)
                    .filter(StringUtils::isNotBlank)
                    .orElse(System.getenv("skillId"));

            Optional.ofNullable(endpointConfiguration.get("lwa")).filter(o -> o instanceof HashMap).map(o -> (HashMap)o).ifPresent(yLwa -> {
                this.lwaClientId = Optional.ofNullable(yLwa.get("clientId")).map(Object::toString).orElse(null);
                this.lwaClientSecret = Optional.ofNullable(yLwa.get("clientSecret")).map(Object::toString).orElse(null);
                this.lwaRefreshToken = Optional.ofNullable(yLwa.get("refreshToken")).map(Object::toString).orElse(null);
            });
        }

        public AlexaApiEndpointBuilder withSkillId(final String skillId) {
            this.skillId = skillId;
            return this;
        }

        public AlexaApiEndpointBuilder withLwaClientId(final String lwaClientId) {
            this.lwaClientId = lwaClientId;
            return this;
        }

        public AlexaApiEndpointBuilder withLwaClientSecret(final String lwaClientSecret) {
            this.lwaClientSecret = lwaClientSecret;
            return this;
        }

        public AlexaApiEndpointBuilder withLwaRefreshToken(final String lwaRefreshToken) {
            this.lwaRefreshToken = lwaRefreshToken;
            return this;
        }

        void preBuild() {
            Validate.notBlank(skillId, "SkillId must not be empty.");
        }

        public abstract T build();
    }

}
