package io.klerch.alexa.test.client;

import org.junit.Assert;
import org.junit.Test;

public class AlexaClientTest {
    @Test
    public void generateUserId() throws Exception {
        final String userId = AlexaClient.generateUserId();
        Assert.assertNotNull(userId);
        Assert.assertTrue(userId.matches("amzn1.ask.account.[A-Z0-9-]{207}"));

        final String userId2 = AlexaClient.generateUserId();
        Assert.assertNotEquals(userId, userId2);
    }

    @Test
    public void generateApplicationId() throws Exception {
        final String applicationId = AlexaClient.generateApplicationId();
        Assert.assertNotNull(applicationId);
        Assert.assertTrue(applicationId.matches("amzn1.ask.skill.[a-z0-9-]{36}"));

        final String applicationId2 = AlexaClient.generateApplicationId();
        Assert.assertNotEquals(applicationId, applicationId2);
    }
}