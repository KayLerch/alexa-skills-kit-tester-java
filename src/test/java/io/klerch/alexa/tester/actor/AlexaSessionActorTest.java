package io.klerch.alexa.tester.actor;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import io.klerch.alexa.tester.AssetFactory;
import io.klerch.alexa.tester.client.AlexaUnitClient;
import io.klerch.alexa.tester.response.AlexaResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlexaSessionActorTest {
    private AlexaSessionActor givenActor() {
        final SpeechletResponseEnvelope envelope = AssetFactory.givenResponseWithSsmlOutputSpeech();
        final AlexaUnitClient client = AlexaUnitClient.create("appId", AssetFactory.givenRequestStreamHandlerThatReturns(envelope)).build();
        return new AlexaSessionActor(client);
    }

    @Test
    public void launch() throws Exception {
        final AlexaResponse response = givenActor().launch();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "launch"));
    }

    @Test
    public void repeat() throws Exception {
        final AlexaResponse response = givenActor().repeat();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.RepeatIntent"));
    }

    @Test
    public void yes() throws Exception {
        final AlexaResponse response = givenActor().yes();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.YesIntent"));
    }

    @Test
    public void no() throws Exception {
        final AlexaResponse response = givenActor().no();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.NoIntent"));
    }

    @Test
    public void cancel() throws Exception {
        final AlexaResponse response = givenActor().cancel();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.CancelIntent"));
    }

    @Test
    public void stop() throws Exception {
        final AlexaResponse response = givenActor().stop();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.StopIntent"));
    }

    @Test
    public void startOver() throws Exception {
        final AlexaResponse response = givenActor().startOver();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.StartOverIntent"));
    }

    @Test
    public void next() throws Exception {
        final AlexaResponse response = givenActor().next();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.NextIntent"));
    }

    @Test
    public void previous() throws Exception {
        final AlexaResponse response = givenActor().previous();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.PreviousIntent"));
    }

    @Test
    public void pause() throws Exception {
        final AlexaResponse response = givenActor().pause();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.PauseIntent"));
    }

    @Test
    public void resume() throws Exception {
        final AlexaResponse response = givenActor().resume();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.ResumeIntent"));
    }

    @Test
    public void shuffleOn() throws Exception {
        final AlexaResponse response = givenActor().shuffleOn();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.ShuffleOnIntent"));
    }

    @Test
    public void shuffleOff() throws Exception {
        final AlexaResponse response = givenActor().shuffleOff();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.ShuffleOffIntent"));
    }

    @Test
    public void loopOn() throws Exception {
        final AlexaResponse response = givenActor().loopOn();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.LoopOnIntent"));
    }

    @Test
    public void loopOff() throws Exception {
        final AlexaResponse response = givenActor().loopOff();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.LoopOffIntent"));
    }

    @Test
    public void help() throws Exception {
        final AlexaResponse response = givenActor().help();
        Assert.assertEquals(response, response.assertSessionStateEquals(AssetFactory.SESSION_KEY_WITH_INTENT_NAME, "AMAZON.HelpIntent"));
    }

    @Test
    public void delay() throws Exception {
        final long delayMillis = 2000;
        final long startMillis = System.currentTimeMillis();
        givenActor().delay(delayMillis);
        final long stopMillis = System.currentTimeMillis();
        Assert.assertTrue((stopMillis - startMillis) >= delayMillis);
    }
}