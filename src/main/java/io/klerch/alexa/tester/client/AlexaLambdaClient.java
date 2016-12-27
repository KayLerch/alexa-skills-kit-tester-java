package io.klerch.alexa.tester.client;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import io.klerch.alexa.tester.request.AlexaRequest;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.Optional;

public class AlexaLambdaClient extends AlexaClient {
    private final static Logger log = Logger.getLogger(AlexaLambdaClient.class);
    private final AWSLambda lambdaClient;
    private final String lambdaFunctionName;
    private long lastExecutionMillis;

    AlexaLambdaClient(final AlexaSystemTestBuilder builder) {
        super(builder);
        this.lambdaClient = builder.lambdaClient;
        this.lambdaFunctionName = builder.lambdaFunctionName;
    }

    public static AlexaSystemTestBuilder create(final String applicationId, final String lambdaFunctionName) {
        return new AlexaSystemTestBuilder(applicationId, lambdaFunctionName);
    }

    public AWSLambda getLambdaClient() {
        return lambdaClient;
    }

    public String getLambdaFunctionName() {
        return lambdaFunctionName;
    }

    @Override
    public long getLastExecutionMillis() {
        return lastExecutionMillis;
    }

    @Override
    public Optional<AlexaResponse> fire(final AlexaRequest request, final String payload) {
        final InvocationType invocationType = request.expectsResponse() ? InvocationType.RequestResponse : InvocationType.Event;
        final InvokeRequest invokeRequest = new InvokeRequest()
                .withInvocationType(invocationType)
                .withFunctionName(lambdaFunctionName)
                .withPayload(payload);
        final long startTimestamp = System.currentTimeMillis();
        log.info(String.format("\t[INFO] Invoke lambda function '%s'.", lambdaFunctionName));
        log.debug(String.format("\t[INFO] with request payload '%s'.", payload));
        final InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
        lastExecutionMillis = System.currentTimeMillis() - startTimestamp;
        return invocationType.equals(InvocationType.RequestResponse) ?
                Optional.of(new AlexaResponse(request, invokeResult.getPayload().array())) : Optional.empty();
    }

    public static class AlexaSystemTestBuilder extends AlexaTestBuilder<AlexaLambdaClient, AlexaSystemTestBuilder> {
        String lambdaFunctionName;
        AWSLambda lambdaClient;

        AlexaSystemTestBuilder(final String applicationId, final String lambdaFunctionName) {
            super(applicationId);
            this.lambdaFunctionName = lambdaFunctionName;
        }

        public AlexaSystemTestBuilder withLambdaClient(final AWSLambda lambdaClient) {
            this.lambdaClient = lambdaClient;
            return this;
        }

        @Override
        public AlexaLambdaClient build() {
            preBuild();
            Validate.notBlank(lambdaFunctionName, "Lambda function name must not be empty.");

            if (lambdaClient == null) {
                lambdaClient = new AWSLambdaClient();
            }

            return new AlexaLambdaClient(this);
        }
    }
}
