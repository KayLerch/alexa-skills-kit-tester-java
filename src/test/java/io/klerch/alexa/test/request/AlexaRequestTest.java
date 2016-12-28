package io.klerch.alexa.test.request;

import org.junit.Assert;
import org.junit.Test;

public class AlexaRequestTest {
    @Test
    public void generateRequestId() throws Exception {
        final String requestId = AlexaRequest.generateRequestId();
        Assert.assertNotNull(requestId);
        Assert.assertTrue(requestId.matches("EdwRequestId.[a-z0-9-]{36}"));

        final String requestId2 = AlexaRequest.generateRequestId();
        Assert.assertNotEquals(requestId, requestId2);
    }
}