package io.klerch.alexa.test.client;

import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class AlexaUnitClient extends AlexaClient {
    private final static Logger log = Logger.getLogger(AlexaLambdaClient.class);
    private final RequestStreamHandler requestStreamHandler;
    private final Context context;
    private long lastExecutionMillis = -1;

    AlexaUnitClient(final AlexaUnitTestBuilder builder) {
        super(builder);
        this.requestStreamHandler = builder.requestStreamHandler;
        this.context = builder.context;
    }

    public static AlexaUnitTestBuilder create(final String applicationId, final RequestStreamHandler requestStreamHandler) {
        return new AlexaUnitTestBuilder(applicationId, requestStreamHandler);
    }

    public RequestStreamHandler getRequestStreamHandler() {
        return requestStreamHandler;
    }

    public Context getContext() { return context; }

    @Override
    public long getLastExecutionMillis() {
        return lastExecutionMillis;
    }

    @Override
    public Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final InputStream inputStream = new ByteArrayInputStream(payload.getBytes());
        try {
            final long startTimestamp = System.currentTimeMillis();
            log.info(String.format("→ [INFO] Call request handler '%s'.", requestStreamHandler.getClass().getCanonicalName()));
            log.debug(String.format("→ [INFO] with request payload '%s'.", payload));
            requestStreamHandler.handleRequest(inputStream, outputStream, context);
            lastExecutionMillis = System.currentTimeMillis() - startTimestamp;
        } catch (final IOException e) {
            final String msg = String.format("Error on invoking request stream handler. %s", e.getMessage());
            log.error(String.format("→ [ERROR] %s", msg));
            throw new RuntimeException(msg, e);
        }
        return request.expectsResponse() ?
                Optional.of(new AlexaResponse(request, outputStream.toByteArray())) : Optional.empty();
    }

    public static class AlexaUnitTestBuilder extends AlexaTestBuilder<AlexaUnitClient, AlexaUnitTestBuilder> {
        RequestStreamHandler requestStreamHandler;
        Context context;

        AlexaUnitTestBuilder(final String applicationId, final RequestStreamHandler requestStreamHandler) {
            super(applicationId);
            this.requestStreamHandler = requestStreamHandler;
        }

        public AlexaUnitTestBuilder withContext(final Context context) {
            this.context = context;
            return this;
        }

        @Override
        public AlexaUnitClient build() {
            preBuild();
            Validate.notNull(requestStreamHandler, "Request stream handler must not be null.");

            if (context == null) {
                context = getContext();
            }

            return new AlexaUnitClient(this);
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
