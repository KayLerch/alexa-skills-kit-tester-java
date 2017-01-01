package io.klerch.alexa.test.client.endpoint;

import com.amazonaws.services.lambda.runtime.Context;
import io.klerch.alexa.test.AssetFactory;
import io.klerch.alexa.test.client.endpoint.samples.AlexaRequestStreamHandler;
import org.junit.Assert;
import org.junit.Test;

public class AlexaRequestStreamHandlerEndpointTest {
    @Test
    public void createFromInstance() throws Exception {
        final AlexaRequestStreamHandler handler = new AlexaRequestStreamHandler();
        final Context context = AssetFactory.givenContext();
        final AlexaRequestStreamHandlerEndpoint arh = AlexaRequestStreamHandlerEndpoint.create(handler).withContext(context).build();

        Assert.assertEquals(arh.getContext(), context);
        Assert.assertEquals(arh.getRequestStreamHandler(), handler);
    }

    @Test
    public void createFromClassReference() throws Exception {
        final Context context = AssetFactory.givenContext();
        final AlexaRequestStreamHandlerEndpoint arh = AlexaRequestStreamHandlerEndpoint.create(AlexaRequestStreamHandler.class).withContext(context).build();

        Assert.assertEquals(arh.getContext(), context);
        Assert.assertNotNull(arh.getRequestStreamHandler());
        Assert.assertTrue(arh.getRequestStreamHandler() instanceof AlexaRequestStreamHandler);
    }

    @Test
    public void createFromClassName() throws Exception {
        final Context context = AssetFactory.givenContext();
        final AlexaRequestStreamHandlerEndpoint arh = AlexaRequestStreamHandlerEndpoint.create(AlexaRequestStreamHandler.class.getCanonicalName()).withContext(context).build();

        Assert.assertEquals(arh.getContext(), context);
        Assert.assertNotNull(arh.getRequestStreamHandler());
        Assert.assertTrue(arh.getRequestStreamHandler() instanceof AlexaRequestStreamHandler);
    }
}