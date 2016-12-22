package io.klerch.alexa.tester.client;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import skill.CalculationSpeechletHandler;

import java.util.Locale;

public class AlexaUnitTestTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void createInvalid() throws Exception {
        exception.expect(NullPointerException.class);
        AlexaUnitTest.create("appId", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitTest.create("", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitTest.create(null, new CalculationSpeechletHandler()).build();
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final CalculationSpeechletHandler handler = new CalculationSpeechletHandler();
        final AlexaUnitTest test1 = AlexaUnitTest.create("appId", handler).build();
        Assert.assertEquals(test1.getLocale(), Locale.US);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertNotNull(test1.getContext());
        Assert.assertNotNull(test1.getSession());
        Assert.assertEquals(test1.getSession().getApplication().getApplicationId(), "appId");
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final CalculationSpeechletHandler handler = new CalculationSpeechletHandler();
        final Context context = getContext();
        final AlexaUnitTest test1 = AlexaUnitTest.create("appId", handler)
                .withLocale(Locale.GERMANY)
                .withUserId("uid")
                .withContext(context)
                .withAccessToken("accessToken")
                .build();
        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertEquals(test1.getContext(), context);
        Assert.assertNotNull(test1.getSession());
        Assert.assertEquals(test1.getSession().getApplication().getApplicationId(), "appId");
        Assert.assertEquals(test1.getSession().getUser().getUserId(), "uid");
        Assert.assertEquals(test1.getSession().getUser().getAccessToken(), "accessToken");
    }

    @Test
    public void fire() throws Exception {

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