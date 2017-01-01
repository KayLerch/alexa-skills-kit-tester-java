package io.klerch.alexa.test.client.endpoint.samples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AlexaRequestHandler implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String s, Context context) {
        return null;
    }
}
