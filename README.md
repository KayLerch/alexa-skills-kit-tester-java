# Alexa Skills Kit Testing Framework
This framework lets you script and execute complex interactions with your Alexa skill 
in order to simulate a user's conversation. 

## Use cases
### Unit & integration testing
If you built your skill with Java you can use this framework to write unit & integration
tests. The _AlexaUnitClient_ is leveraged to fire requests at your _RequestStreamHandler_ implementation
 class. The resulting _AlexaResponse_ object provides a lot of methods to validate your skill's response
 with making assertions. The fluent interface not only looks beautiful but is easy to understand
 and perfect to script your conversation without being too technical.

##### Code a skill conversation in Java
```java
@Test
public void doConversation() throws Exception {
    final AlexaClient client = AlexaUnitClient
        .create("applicationId", new MyRequestStreamHandler())
        .build();
        
    client.startSession() // SessionStartedRequest
        .launch() // LaunchRequest
            .assertThat(response -> response.getVersion().equals("1.0"))
            .assertTrue(AlexaAssertion.HasCardIsSimple)
            .assertExecutionTimeLessThan(1000)
            .done()
        .intent("myIntent", "slot1", true) // IntentRequest with custom intent
            .assertSessionStillOpen()
            .assertSessionStateEquals("slot1", "true")
            .assertMatches(AlexaAsset.OutputSpeechSsml, ".*hello.*")
            .done()
        .delay(1000)
        .repeat() // IntentRequest with builtin AMAZON.RepeatIntent
            .assertMatches(AlexaAsset.OutputSpeechSsml, ".*hello again.*")
            .done()
        .endSession(); // SessionEndedRequested
    }
```
The above unit tests fires five speechlet requests at your skill and validates
their outputs. 

##### Design a skill conversation in XML
Tired of coding tests? You can also script your conversation in XML and leverage
_AlexaLambdaScriptClient_ to execute it against your skill Lambda function.

```xml
<test>
    <configuration>
        <endpoint>myLambdaFunctionName</endpoint>
        <locale>de-DE</locale>
        <application id="myApplicationId" />
    </configuration>
    <sessions>
        <session>
            <launch>
                <assertTrue assertion="HasOutputSpeech" />
                <assertFalse assertion="HasOutputSpeechIsPlainText" />
                <assertEquals asset="StandardCardTitle" value="card_title" />
            </launch>
            <delay value="1000" />
            <intent name="intent1">
                <request>
                    <slots>
                        <slot key="slot1" value="val" />
                    </slots>
                </request>
                <assertSessionStateEquals key="slot1" value="val" />
            </intent>
            <yes>
                <assertMatches asset="OutputSpeechSsml" value=".*test.*" />
            </yes>
        </session>
        <session>
            <help>
                <assertTrue assertion="HasOutputSpeechIsSsml" />
            </help>
        </session>
    </sessions>
</test>
```
Have a look at a rich example file [here](/src/test/resources/script-max.xml) and use the 
[schema file](/src/main/resources/testScript.xsd) to script your own conversations.

To execute the XML script you need to do the following:

```java
public class MyTestLambdaFunction implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        final AlexaLambdaScriptClient client = AlexaLambdaScriptClient
                        .create(URI.create("https://url.to/testscript.xml"))
                        .build();
        client.startScript();
    }
}
```

### Live testing
When your skill is in production it is even more important to know if everything
is doing fine. In case your skill runs in a Lambda function you are good to go with this 
framework to establish an early-warning-system for potential Alexa skill outages. 
Just think of a second Lambda function running the above code against your
live skill.

```java
public class MyTestLambdaFunction implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        final AlexaClient client = AlexaLambdaClient
                .create("applicationId", "lambda-function-name") 
                .withLocale(Locale.US)
                .withUserId("myUserId")
                .build();
            // ...
    }
}
```

That Lambda function could be scheduled to run every x minutes. As all the
 _assert_-methods will throw runtime exceptions if the assertion is false that Lambda function
 would indicate suspicious moments with runtime errors.
 
![](docs/live-testing.png)

1. A CloudWatch rule triggers a Lambda function that hosts the above test-code. This could be 
scheduled to every minute or just once a day.

2. The Lambda function would either execute the test-code directly or loads
 a test-script file (XML) stored in an S3 bucket or elsewhere. 
 
3. According to the test script the framework sends speechlet requests to the original skill
endpoint the same way Alexa service does on behalf of user requests from an Echo device. As the test
client manages session state multiple requests are covered under the hood of a single session. That's
how you could even simulate complex conversations between users and your skill. The skill Lambda function
won't recognize the difference unless you make use of an option to set a specific session attribute flag.

4. The skill Lambda function replies with a JSON speechlet response that is validated against certain 
expectations and assertions defined in your test-script. This would be the content of the reprompt, the 
existence of a card or the absence of a directive. You could even expect a maximum execution time. 
If only one of those assertions is wrong the test client throws a runtime validation exception causing the
Lambda function to return with a failure.

5. CloudWatch metrics keep track of all the invocation results. You can set an alarm if CloudWatch metrics
hit your custom-defined thresholds.

6. CloudWatch alarms can expose information to subscribers in SNS - possibly an email- or SMS-distribution-list.
This is how a test failure reaches you or your IT staff.

There you got your early-warning-system for Alexa skill outages.