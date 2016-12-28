package io.klerch.alexa.test.automation;
import io.klerch.alexa.test.actor.AlexaSessionActor;
import io.klerch.alexa.test.client.AlexaClient;
import io.klerch.alexa.test.client.AlexaLambdaClient;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static org.joox.JOOX.*;

public class AlexaTestScript {
    private final static Logger log = Logger.getLogger(AlexaTestScript.class);

    public static void execute(final InputStream inputStream) throws IOException, SAXException {
        // Parse the document from a file
        final Document document = $(inputStream).document();

        // root
        final Match mTest = $(document).find("test");
        Validate.isTrue(mTest.isNotEmpty(), "Root node 'test' not found.");

        // configuration
        final Match mConfig = mTest.find("configuration");
        Validate.isTrue(mConfig.isNotEmpty(), "Node 'configuration' not found.");

        final AlexaClient client = AlexaLambdaClient.create(mConfig).build();

        // sessions
        final Match mSessions = mTest.find("sessions");
        if (mSessions.isEmpty()) {
            log.warn("No test-sessions defined in script-file. Nothing is executed in this test-run.");
            return;
        }

        mSessions.find("session").forEach(session -> {
            final AlexaSessionActor actor = client.startSession();

            $(session).children().forEach(request -> {
                try {
                    final AlexaResponse response = (AlexaResponse)AlexaSessionActor.class.getMethod(request.getTagName()).invoke(actor);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    final String msg = String.format("The request '%1$s' in your script-file is not supported. %2$s", request.getTagName(), e.getMessage());
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            });

            actor.endSession();
        });
    }
}
