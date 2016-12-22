package io.klerch.alexa.tester.client;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import io.klerch.alexa.tester.request.AlexaRequest;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.apache.commons.lang3.Validate;

public class AlexaSystemTest extends AlexaTest {
    private final AWSLambda lambdaClient;
    private final String lambdaFunctionName;

    AlexaSystemTest(final AlexaSystemTestBuilder builder) {
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
    public AlexaResponse fire(final AlexaRequest request, final String payload) {
        final InvocationType invocationType = request.expectsResponse() ? InvocationType.RequestResponse : InvocationType.Event;
        final InvokeRequest invokeRequest = new InvokeRequest()
                .withInvocationType(invocationType)
                .withFunctionName(lambdaFunctionName)
                .withPayload(payload);
        final InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
        return invocationType.equals(InvocationType.RequestResponse) ?
                new AlexaResponse(request, invokeResult.getPayload().array()) : AlexaResponse.VOID;
    }

    public static class AlexaSystemTestBuilder extends AlexaTestBuilder<AlexaSystemTest, AlexaSystemTestBuilder> {
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
        public AlexaSystemTest build() {
            preBuild();
            Validate.notBlank(lambdaFunctionName, "Lambda function name must not be empty.");

            if (lambdaClient == null) {
                lambdaClient = new AWSLambdaClient();
            }

            return new AlexaSystemTest(this);
        }
    }
}
