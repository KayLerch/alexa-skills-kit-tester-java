package io.klerch.alexa.test.client.endpoint;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import io.klerch.alexa.test.request.AlexaRequest;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.Optional;

public class AlexaLambdaEndpoint implements AlexaEndpoint {
    private final static Logger log = Logger.getLogger(AlexaLambdaEndpoint.class);
    private final AWSLambda lambdaClient;
    private final String lambdaFunctionName;

    AlexaLambdaEndpoint(final AlexaLambdaEndpointBuilder builder) {
        this.lambdaClient = builder.lambdaClient;
        this.lambdaFunctionName = builder.lambdaFunctionName;
    }

    public String getLambdaFunctionName() {
        return this.lambdaFunctionName;
    }

    public AWSLambda getLambdaClient() {
        return this.lambdaClient;
    }

    public Optional<AlexaResponse> fire(AlexaRequest request, String payload) {
        final InvocationType invocationType = request.expectsResponse() ? InvocationType.RequestResponse : InvocationType.Event;
        final InvokeRequest invokeRequest = new InvokeRequest()
                .withInvocationType(invocationType)
                .withFunctionName(lambdaFunctionName)
                .withPayload(payload);
        log.info(String.format("->[INFO] Invoke lambda function '%s'.", lambdaFunctionName));
        log.debug(String.format("->[INFO] with request payload '%s'.", payload));
        final InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
        return invocationType.equals(InvocationType.RequestResponse) ?
                Optional.of(new AlexaResponse(request, payload, new String(invokeResult.getPayload().array()))) : Optional.empty();
    }

    public static AlexaLambdaEndpointBuilder create(final String lambdaFunctionName) {
        return new AlexaLambdaEndpointBuilder(lambdaFunctionName);
    }

    public static class AlexaLambdaEndpointBuilder {
        String lambdaFunctionName;
        AWSLambda lambdaClient;

        AlexaLambdaEndpointBuilder(final String lambdaFunctionName) {
            this.lambdaFunctionName = lambdaFunctionName;
        }

        public AlexaLambdaEndpointBuilder withLambdaClient(final AWSLambda lambdaClient) {
            this.lambdaClient = lambdaClient;
            return this;
        }

        public AlexaLambdaEndpoint build() {
            Validate.notBlank(lambdaFunctionName, "Lambda function name must not be empty.");

            if (lambdaClient == null) {
                lambdaClient = new AWSLambdaClient();
            }

            return new AlexaLambdaEndpoint(this);
        }
    }
}
