package io.klerch.alexa.test.client;

import io.klerch.alexa.test.actor.AlexaSessionActor;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;

import static org.joox.JOOX.$;

public class AlexaLambdaScriptClient extends AlexaLambdaClient {
    private final static Logger log = Logger.getLogger(AlexaLambdaScriptClient.class);
    private final Match mSessions;

    AlexaLambdaScriptClient(final AlexaLambdaScriptClientBuilder builder) {
        super(builder);
        mSessions = builder.mSessions;
    }

    public void startScript() {
        if (mSessions.isEmpty()) {
            log.warn("No test-sessions defined in script-file. Nothing is executed in this test-run.");
            return;
        }

        mSessions.find("session").forEach(session -> {
            final AlexaSessionActor actor = this.startSession();

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

    public static AlexaLambdaScriptClientBuilder create(final URI uri) throws IOException, SAXException {
        return create($(uri).document());
    }

    public static AlexaLambdaScriptClientBuilder create(final URL url) throws IOException, SAXException {
        return create($(url).document());
    }

    public static AlexaLambdaScriptClientBuilder create(final InputStream inputStream) throws IOException, SAXException {
        return create($(inputStream).document());
    }

    public static AlexaLambdaScriptClientBuilder create(final File file) throws IOException, SAXException {
        return create($(file).document());
    }

    private static AlexaLambdaScriptClientBuilder create(final Document document) {
        // root
        final Match mTest = $(document).first();
        Validate.isTrue(mTest.isNotEmpty(), "Root node 'test' not found.");

        // configuration
        final Match mConfig = mTest.find("configuration");
        Validate.isTrue(mConfig.isNotEmpty(), "Node 'configuration' not found.");

        final Match mApplication = mConfig.find("application");
        Validate.isTrue(mApplication.isNotEmpty(), "Node 'application' not found.");

        final Match mEndpoint = mConfig.find("endpoint");
        Validate.isTrue(mEndpoint.isNotEmpty(), "Node 'endpoint' not found.");

        final Match mSessions = mTest.find("sessions");

        return new AlexaLambdaScriptClientBuilder(mApplication.id(), mEndpoint.text(), mConfig, mSessions);
    }

    public static class AlexaLambdaScriptClientBuilder extends AlexaLambdaClientBuilder<AlexaLambdaScriptClient, AlexaLambdaScriptClientBuilder> {
        final Match mSessions;

        AlexaLambdaScriptClientBuilder(final String applicationId, final String lambdaFunctionName, final Match mConfig, final Match mSessions) {
            super(applicationId, lambdaFunctionName);

            this.mSessions = mSessions;

            final Match mLocale = mConfig.find("locale");
            if (mLocale.isNotEmpty()) {
                withLocale(mLocale.text());
            }

            final Match mDebugFlag = mConfig.find("debugFlagSessionAttribute");
            if (mDebugFlag.isNotEmpty()) {
                withDebugFlagSessionAttribute(mDebugFlag.text());
            }

            final Match mUser = mConfig.find("user");
            if (mUser.isNotEmpty()) {
                withUserId(mUser.id());
                withAccessToken(mUser.attr("accessToken"));
            }
        }

        @Override
        public AlexaLambdaScriptClient build() {
            preBuild();
            return new AlexaLambdaScriptClient(this);
        }
    }
}
