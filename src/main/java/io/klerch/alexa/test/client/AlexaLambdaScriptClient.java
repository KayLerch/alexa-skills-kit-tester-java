package io.klerch.alexa.test.client;

import com.amazon.speech.slu.Slot;
import io.klerch.alexa.test.asset.AlexaAssertion;
import io.klerch.alexa.test.asset.AlexaAsset;
import io.klerch.alexa.test.response.AlexaResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.joox.Match;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import static org.joox.JOOX.$;

public class AlexaLambdaScriptClient extends AlexaLambdaClient {
    private final static Logger log = Logger.getLogger(AlexaLambdaScriptClient.class);
    private final Match mSessions;
    private AlexaSessionActor actor;

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
            actor = this.startSession();
            $(session).children().forEach(processActionInSession);
            actor.endSession();
        });
    }

    private Consumer<Element> processActionInSession = request -> {
        final String requestName = request.getLocalName();
        // in case request is a delay, must provide a value as attribute
        Validate.isTrue(request.hasAttribute("value") || !"delay".equals(requestName), "[ERROR] Delay must have a value provided as an attribute of delay-tag in your script-file. The value should be numeric and indicates the milliseconds to wait for the next request.");
        // in case request is a custom intent, must provide a name as attribute
        Validate.isTrue(request.hasAttribute("name") || !"intent".equals(requestName), "[ERROR] Intent must have a name provided as an attribute of intent-tag in your script-file.");
        // request must match method in actor
        Validate.notNull(Arrays.stream(AlexaSessionActor.class.getMethods()).anyMatch(method -> method.getName().equals(requestName)), "[ERROR] Unknown request-type '%s' found in your script-file.", requestName);

        if ("delay".equals(requestName)) {
            actor.delay(Long.parseLong(request.getAttribute("value")));
        } else {
            try {
                // request the skill with intent
                final AlexaResponse response = "intent".equals(requestName) ?
                        actor.intent(request.getAttribute("name"), extractSlots($(request))) :
                        (AlexaResponse)AlexaSessionActor.class.getMethod(requestName).invoke(actor);
                // validate response
                validateResponse(response, $(request));
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                final String msg = String.format("[ERROR] The request '%1$s' in your script-file is not supported. %2$s", request.getTagName(), e.getMessage());
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }
    };


    private void validateResponse(final AlexaResponse response, final Match mResponse) {
        if (mResponse.isEmpty()) {
            log.info("[INFO] No assertions defined in your script-file to validate skill's response.");
            return;
        }

        mResponse.children().forEach(assertion -> {
            final String assertionMethod = assertion.getTagName();
            final String assetName = assertion.getAttribute("asset");
            final String assertionName = assertion.getAttribute("assertion");
            final String key = assertion.getAttribute("key");
            final String value = assertion.getAttribute("value");

            // skip request node which is at the same level as all the assertion-tags
            if ("request".equals(assertionMethod)) return;

            //Validate.matchesPattern(assertionMethod, "assert.*", "[ERROR] Invalid assertion method '%s' in your script-file.", assertionMethod);
            Validate.isTrue(Arrays.stream(response.getClass().getMethods()).anyMatch(m -> m.getName().equals(assertionMethod)), "[ERROR] Invalid assertion method '%s' in your script-file.", assertionMethod);

            Arrays.stream(response.getClass().getMethods())
                    .filter(m -> m.getName().equals(assertionMethod))
                    .findFirst()
                    .ifPresent(m -> {
                        final List<Object> parameters = new ArrayList<>();
                        if (StringUtils.containsIgnoreCase(m.getName(), "SessionState")) {
                            if (m.getParameterCount() > 0) {
                                parameters.add(key);
                            }
                            if (m.getParameterCount() > 1) {
                                parameters.add(value);
                            }
                        } else {
                            Arrays.stream(m.getParameterTypes()).forEach(pc -> {
                                // assign value to parameter based on its type
                                parameters.add(AlexaAsset.class.equals(pc) ?
                                        Enum.valueOf(AlexaAsset.class, assetName) :
                                        AlexaAssertion.class.equals(pc) ?
                                                Enum.valueOf(AlexaAssertion.class, assertionName) :
                                                long.class.equals(pc) ?
                                                        Long.parseLong(value) :
                                                        pc.cast(value));
                            });
                        }
                        try {
                            final Object result = m.invoke(response, parameters.toArray());
                            // result only for conditional methods and those are always booleans
                            if (result != null && Boolean.parseBoolean(result.toString())) {
                                // in that case a conditional was true. The body should contain actions to perform (recursion starts)
                                $(assertion).children().forEach(processActionInSession);
                            }
                        } catch (final InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
        });
    }

    private List<Slot> extractSlots(final Match mRequest) {
        final List<Slot> slots = new ArrayList<>();
        mRequest.find("request").find("slots").find("slot").forEach(mSlot -> {
            if (mSlot.hasAttribute("key") && mSlot.hasAttribute("value")) {
                slots.add(Slot.builder()
                        .withName(mSlot.getAttribute("key"))
                        .withValue(mSlot.getAttribute("value")).build());
            }
        });
        return slots;
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

            final Match mTimestamp = mConfig.find("timestamp");
            if (mTimestamp.isNotEmpty()) {
                withTimestamp(new Date(mTimestamp.text(Timestamp.class).getTime()));
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
