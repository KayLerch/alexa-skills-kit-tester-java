import io.klerch.alexa.tester.client.AlexaTest;
import io.klerch.alexa.tester.client.AlexaUnitTest;
import skill.CalculationSpeechletHandler;

import java.util.Locale;

import static io.klerch.alexa.tester.asset.AlexaAssertion.HasOutputSpeech;
import static io.klerch.alexa.tester.asset.AlexaAssertion.HasReprompt;

public class Assets {
    @org.junit.Test
    public void test() {
        final CalculationSpeechletHandler handler = new CalculationSpeechletHandler();
        final AlexaTest test = AlexaUnitTest.create("amzn1.ask.skill.xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", handler)
                .withLocale(Locale.GERMANY).build();

        test.start()
                .launch()
                    .assertTrue(HasOutputSpeech)
                    .assertTrue(HasReprompt)
                    .assertSessionOpen()
                    .done()
                .end();
    }
}
