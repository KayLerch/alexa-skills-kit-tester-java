package io.klerch.alexa.test.client.endpoint;

import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class AlexaRequestStreamHandlerEndpoint implements AlexaEndpoint {
    private final static Logger log = Logger.getLogger(AlexaRequestStreamHandlerEndpoint.class);
    private final RequestStreamHandler requestStreamHandler;
    private final Context context;

    AlexaRequestStreamHandlerEndpoint(final AlexaRequestStreamHandlerEndpointBuilder builder) {
        this.requestStreamHandler = builder.requestStreamHandler;
        this.context = builder.context;
    }

    public RequestStreamHandler getRequestStreamHandler() {
        return this.requestStreamHandler;
    }

    public Context getContext() {
        return this.context;
    }

    public Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final InputStream inputStream = new ByteArrayInputStream(payload.getBytes());
        try {
            log.info(String.format("->[INFO] Call request handler '%s'.", requestStreamHandler.getClass().getCanonicalName()));
            log.debug(String.format("->[INFO] with request payload '%s'.", payload));
            requestStreamHandler.handleRequest(inputStream, outputStream, context);
        } catch (final IOException e) {
            final String msg = String.format("Error on invoking request stream handler. %s", e.getMessage());
            log.error(String.format("->[ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        return request.expectsResponse() ?
                Optional.of(new AlexaResponse(request, payload, new String(outputStream.toByteArray()))) : Optional.empty();
    }

    static AlexaRequestStreamHandlerEndpointBuilder create(final HashMap<Object, Object> endpointConfiguration) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Validate.notEmpty(endpointConfiguration, "Endpoint configuration must not be empty. At least the class-attribute is necessary for the RequestStreamHandler.");
        Validate.isTrue(endpointConfiguration.containsKey("class"), "class-attribute is missing in your Endpoint configuration for the RequestStreamHandler.");

        return create(Class.forName(endpointConfiguration.get("class").toString()).asSubclass(RequestStreamHandler.class));
    }

    public static <T extends RequestStreamHandler> AlexaRequestStreamHandlerEndpointBuilder create(final Class<T> clazz) throws IllegalAccessException, InstantiationException {
        return create(clazz.newInstance());
    }

    public static AlexaRequestStreamHandlerEndpointBuilder create(final RequestStreamHandler streamHandler) {
        return new AlexaRequestStreamHandlerEndpointBuilder(streamHandler);
    }

    public static class AlexaRequestStreamHandlerEndpointBuilder {
        RequestStreamHandler requestStreamHandler;
        Context context;

        AlexaRequestStreamHandlerEndpointBuilder(final RequestStreamHandler requestStreamHandler) {
            this.requestStreamHandler = requestStreamHandler;
        }

        public AlexaRequestStreamHandlerEndpointBuilder withContext(final Context context) {
            this.context = context;
            return this;
        }

        public AlexaRequestStreamHandlerEndpoint build() {
            Validate.notNull(requestStreamHandler, "Request stream handler must not be null.");

            if (context == null) {
                context = getContext();
            }

            return new AlexaRequestStreamHandlerEndpoint(this);
        }

        private Context getContext() {
            return new Context() {
                @Override
                public String getAwsRequestId() {
                    return null;
                }

                @Override
                public String getLogGroupName() {
                    return null;
                }

                @Override
                public String getLogStreamName() {
                    return null;
                }

                @Override
                public String getFunctionName() {
                    return null;
                }

                @Override
                public CognitoIdentity getIdentity() {
                    return null;
                }

                @Override
                public ClientContext getClientContext() {
                    return null;
                }

                @Override
                public int getRemainingTimeInMillis() {
                    return 0;
                }

                @Override
                public int getMemoryLimitInMB() {
                    return 0;
                }

                @Override
                public LambdaLogger getLogger() {
                    return null;
                }
            };
        }
    }
}
