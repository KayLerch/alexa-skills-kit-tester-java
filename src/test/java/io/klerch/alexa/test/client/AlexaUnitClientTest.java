package io.klerch.alexa.test.client;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazonaws.services.lambda.runtime.*;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Locale;
import java.util.Optional;

public class AlexaUnitClientTest extends AlexaClientTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    public AlexaClient givenClient() throws Exception {
        return AlexaUnitClient
                .create(AlexaClient.generateApplicationId(), givenHandler())
                .withDebugFlagSessionAttribute("myDebugFlag")
                .build();
    }

    @Test
    public void createInvalid() throws Exception {
        exception.expect(NullPointerException.class);
        AlexaUnitClient.create("appId", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitClient.create("", null).build();

        exception.expect(IllegalArgumentException.class);
        AlexaUnitClient.create(null, AssetFactory.getRequestStreamHandler()).build();
    }

    @Test
    public void createValidMinCustomized() throws Exception {
        final RequestStreamHandler handler = AssetFactory.getRequestStreamHandler();
        final AlexaUnitClient test1 = AlexaUnitClient.create("appId", handler).build();
        Assert.assertEquals(test1.getLocale(), Locale.US);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertNotNull(test1.getContext());
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
    }

    @Test
    public void createValidMaxCustomized() throws Exception {
        final RequestStreamHandler handler = AssetFactory.getRequestStreamHandler();
        final Context context = givenContext();
        final AlexaUnitClient test1 = AlexaUnitClient.create("appId", handler)
                .withContext(context)
                .withLocale(Locale.GERMANY)
                .withUserId("uid")
                .withAccessToken("accessToken")
                .withDebugFlagSessionAttribute("debug123flag")
                .build();
        Assert.assertEquals(test1.getLocale(), Locale.GERMANY);
        Assert.assertEquals(test1.getRequestStreamHandler(), handler);
        Assert.assertEquals(test1.getContext(), context);
        Assert.assertEquals(test1.getDebugFlagSessionAttributeName(), Optional.of("debug123flag"));
        Assert.assertEquals(test1.getApplication().getApplicationId(), "appId");
        Assert.assertEquals(test1.getUser().getUserId(), "uid");
        Assert.assertEquals(test1.getUser().getAccessToken(), "accessToken");
    }
}