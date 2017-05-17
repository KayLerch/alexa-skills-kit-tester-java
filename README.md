[![Join the chat at https://gitter.im/alexa-skills-kit-tester-java/Lobby](https://badges.gitter.im/alexa-skills-kit-tester-java/Lobby.svg)](https://gitter.im/alexa-skills-kit-tester-java/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven central](https://img.shields.io/badge/maven%20central-v0.1.0-orange.svg)](http://search.maven.org/#artifactdetails%7Cio.klerch%7Calexa-skills-kit-tester-java%7C0.1.0%7Cjar)
![SonarQube Coverage](https://img.shields.io/badge/code%20coverage-88%25-green.svg)


# Alexa Skills Kit Testing Framework

This framework lets you script and execute complex conversations with your Alexa skills. Scripts
are expressed in either Java-Code or XML and can be used to automate functional tests or to perform
test-driven development of Alexa skills.
 
### Alexa-Client 
The framework creates an _AlexaClient_ that simulates the Alexa-service sitting between your skill and an Alexa
device. It takes clientside configuration like the SkillId, UserId and Token and the locale.

### The Skill-Endpoint
An _AlexaClient_ needs an endpoint to fire requests at. The endpoint is a gate to the implementation of your Alexa skill
and is one of the following:
- An already existing and running Lambda function in AWS.
- A Java-class deriving from _RequestStreamHandler_ interface of AWS SDK. Referencing a Java-object
only makes sense when you write tests that execute while compiling and packaging a Java skill project.

```java
final AlexaEndpoint lambdaEndpoint = AlexaLambdaEndpoint.create("lamdaFunctionName").build();
final AlexaClient lambdaClient = AlexaClient.create(lambdaEndpoint, "appId")
    .withLocale(Locale.GERMANY)
    .withUserId("userId")
    // let your skill believe it's day after tomorrow
    .withTimestamp(DateUtils.addDays(new Date(), 2))
    .build();
        
AlexaEndpoint unitEndpoint = AlexaRequestStreamHandlerEndpoint.create(MyRequestHandler.class).build();
final AlexaClient unitClient = AlexaClient.create(unitEndpoint).build();
```

### The Script
Now you can give one of these _AlexaClients_ a conversation it should have with an Alexa skill sitting behind
the configured _AlexaEndpoint_. You could either script skill conversations in XML or with the fluent interface
of the _AlexaClient_-object. Before we have a look at a script it's worth explaining the parts of it.
 
##### Actions
An action simply is one request that is send to your skill. Usually this is a _Speechlet-Request_ containing
an _Intent_ or _Event_. Builtin-Intents like the _AMAZON.HelpIntent_ got their own actions as well as the
_LaunchRequest_, the _SessionStarted_- and _SessionEndedRequests_. Custom intents got the generic _intent_-action
that is customizable with a name and slots.

##### Assertions
Once an _Action_ is performed by the _AlexaClient_ and your skill returned the JSON response you want to
validate its contents. Numerous _assert_-methods can be used to make assertions and validate values of _assets_
contained in the response. Note, that _assert_-methods throw runtime exceptions. Assertions you define must be true otherwise
you script won't continue to run. A failed assertion lets the whole test fail and usually indicates an unexpected
 behavior of you skill - something that needs your attention.

##### Assets
An asset is an element inside the skill response. This can be the output-speech, a reprompt, card, directive or session attribute.
Those assets have values you can validate with the _assert_-methods as well. 

##### Conditionals
A condition really much does the same as an _assertion_ but with one difference. It only validates without
throwing an exception. Therefor, you can use _conditionals_ to make decisions while having the conversation and
go into different directions the same way a user would do when using your skill. That said, scripts not only
follow static routes but can also have variable paths - where all of them will be valid unless none of them
fails one of its subsequent assertions. Each _assertion_ has an equivalent _conditional_-method.

```xml
<?xml version="1.0"?>
<script>
    <configuration>
        <endpoint type="Lambda">myLambdaFunctionName</endpoint>
        <locale>de-DE</locale>
        <application id="myApplicationId" />
    </configuration>
    <sessions>
        <session>
            <launch> <!-- ACTION -->
                <assertTrue assertion="HasCardIsSimple"/> <!-- ASSERTION -->
                <assertMatches asset="OutputSpeechPlainText" value="Hello.*"/> <!-- ASSERTION -->
                <sessionStateEquals key="knownUser" value="true"> <!-- CONDITIONAL -->
                    <intent name="myIntent"> <!-- ACTION -->
                        <request>
                            <slots><slot key="mySlot" value="someValue" /></slots>
                        </request>
                        <assertExecutionTimeLessThan value="1000" /> <!-- ASSERTION -->
                        <assertFalse assertion="HasDirectiveIsPlay"/> <!-- ASSERTION -->
                    </intent>
                </sessionStateEquals>
                <sessionStateEquals key="knownUser" value="false"> <!-- CONDITIONAL -->
                    <help> <!-- ACTION -->
                        <assertTrue assertion="HasOutputSpeechIsSsml"/> <!-- ASSERTION -->
                        <assertSessionEnded /> <!-- ASSERTION -->
                    </help>
                </sessionStateEquals>
            </launch>
        </session>
    </sessions>
</script>
```
This is a short script starting with a _LaunchRequest_ that is expected to return a _SimpleCard_ 
and a plain-text saying Hello ... 
Depending on the session-attribute _knownUser_ this session continues with either a custom intent
or a help-intent. A _conditional_-tag can contain more _actions_ that can contain their own _assertions_ and _conditionals_.
 You can nest these structures as deep as you like allowing you to have really complex and long conversations. 

See more example scripts [here](/src/test/resources/) and use this 
[xml schema file](/src/main/resources/testScript.xsd) to create and validate your own test-scripts.

Maybe you noticed the _configuration_-section in the script-XML. It can be used to configure an _AlexaClient_
with all the settings having an impact on how requests are fired at your skill. Store you script-files
wherever you want - in a JAR, S3-bucket or on your file-server. Simply reference the file when you instantiate 
 an _AlexaClient_ and call _startScript_ to execute the conversations defined in the XML.

```java
final AlexaClient client = AlexaClient
        .create(new URL("https://url.to/your/script.xml"))
        .build();
client.startScript();
```

Now think about having these two lines of code in a separate Lambda-function and see what's the
benefit here:

### Live-Testing

This "test"-Lambda function could be scheduled to run every X minutes. As all the
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

### Unit & integration testing
If you built your skill with Java you can use this framework to write unit & integration
tests. Instead of referencing a Lambda-function you point directly to the Lambda implementation class -
  a child of _RequestStreamHandler_.
  
```xml
<?xml version="1.0"?>
<script>
    <configuration>
        <endpoint type="RequestStreamHandler">my.package.MyRequestHandler</endpoint>
        <locale>en-US</locale>
        <application id="myApplicationId" />
    </configuration>
    <sessions />
</script>
```

### The fluent interface 

When you set up your _AlexaClient_ in Java and you don't want to have a script in XML you go for
the fluent API of this test framework. It not only looks beautiful but is easy to understand
and perfect to script your conversation without being too technical.

Instead of calling _startScript_ you begin a session with _startSession_. It returns a new object -
the _AlexaSessionActor_ which provides all _actions_ you already got to know from the XML-script. Those
_actions_ return an _AlexaResponse_-object that got methods for each _assertion_ and _conditional_ introduced
before. Keep in mind that _assert_-methods throw runtime-exceptions - making them ideal for unit-tests.
The _done_ method of _AlexaResponse_ returns the _AlexaSessionActor_ to let you continue with the next _action_
without breaking the fluent API.

```java
AlexaEndpoint unitEndpoint = AlexaRequestStreamHandlerEndpoint.create(MyRequestHandler.class).build();
final AlexaClient unitClient = AlexaClient.create(unitEndpoint).build();
unitClient.startSession() // SessionStartedRequest
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

We haven't covered _delay_ so far which can be useful if your skill makes time-dependant decisions.
_delay_ just sleeps for an amount of milliseconds before going on with the next _action_.


The client takes configuration values which go into Alexa _Speechlet_-requests (UserId, AccessToken, Locale). If you don't
set these values locale defaults to en-US, userId is generated and accessToken left empty. The _DebugFlagSessionAttribute_
optionally defines the name for a session attribute that goes into every request so skills are able to distinguish between
a test invocation and a user call. If you wonder why it's useful to manipulate the timestamp, think of a skill
whose behavior depends on the day of a week or the time of a day. You could run your test scripts while letting
your skill think it's Monday morning - e.g. for asking the user how the weekend was. 

### AlexaSessionActor

Every session in Alexa starts with the _SessionStartedRequest_. That why you initiate a new session
with the _startSession_-method of _AlexaClient_. It returns an instance of _AlexaSessionActor_ that got
all of the other methods you need to fire _LaunchRequests_ and _IntentRequests_.
 
```java
final AlexaSessionActor actor = client.startSession();
```
_AlexaSessionActor_ has methods for each request-type and builtin-intent type. None of them expect
 any arguments except for the _intent_-method that takes slots and a name for custom intents. Let's
 say we start with a _LaunchRequest_. 

```java
final AlexaResponse response = actor.launch();
```

### AlexaResponse

All these _action_-methods return an _AlexaResponse_ instance which not only holds the actual response envelope
of your skill but provides a bunch of _assert_-methods you can use to validate the response. You can
define expectations for all kinds of _assets_ like output-speech, reprompts, cards and directives:

```java
response.assertExists(AlexaAsset.RepromptSpeechSsml);
response.assertEquals(AlexaAsset.DirectivePlayAudioItemStreamOffset, "1000");
response.assertMatches(AlexaAsset.SimpleCardTitle, ".*title.*");
```
 
or you can ask for an assertion to be true or false:

```java
response.assertTrue(AlexaAssertion.HasCardIsStandard);
response.assertFalse(AlexaAssertion.SessionEnded);
```

or you can directly access the response envelope and express your own predicate:

```java
response.assertThat(speechletResponseEnvelope -> speechletResponseEnvelope.getVersion().startsWith("1"));
```

There are dedicated _assert_-methods for session attribute validation:

```java
response.assertSessionStateExists("sessionKey1");
response.assertSessionStateNotNull("sessionKey1");
response.assertSessionStateNotBlank("sessionKey1");
response.assertSessionStateEquals("sessionKey1", "expectedValue");
response.assertSessionStateContains("sessionKey1", "pectedVa");
response.assertSessionStateMatches("sessionKey1", "expected.[5]");
```

Last but not least there is an option to set a threshold for the execution time:

```java
response.assertExecutionTimeLessThan(2500);
```

_Assert_-methods throw runtime exceptions in case the made assertion is not true. If you want
to validate contents of the response without causing exceptions you can make use of _conditional_-methods 
provided by the response-object. Every _assert_-method has an equivalent _conditional_-method whose
return value is a Boolean. Now you are able to have dynamic conversation paths and make decisions based
 on skill responses.
 
```java
final AlexaResponse response2 = actor.intent("myIntent", "mySlot", "mySlotValue");
if (response2.equals(AlexaAsset.OutputSpeechPlainText, "Hi")) {
    final AlexaResponse response3 = actor.help();
}
```

In a fancy one-liner you can simulate a whole conversation a user might have:

```java
actor.launch().done().help().done().intent("myIntent").done().yes().done().endSession();
```

### The log output

To dig into test results and investigate potential errors this test framework
writes logs that are easy to read. Here's an example:

```text
[START] session start request with sessionId 'SessionId.567f262e-de60-4432-90bf-dcd78992db86' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":true,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"SessionStartedRequest","requestId":"EdwRequestId.46eef984-b04a-477f-8dad-18c289bb441a","timestamp":"2010-09-30T11:11:11Z","locale":"de-DE"}}'.
[DONE] session start request.

[START] launch request ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"LaunchRequest","requestId":"EdwRequestId.01cab886-b323-4371-9d09-94b2f3979bc7","timestamp":"2010-09-30T11:11:11Z","locale":"de-DE"}}'.
[DONE] launch request in 10 ms.
->[TRUE] SessionEnded is NOT true.
->[TRUE] HasCard is TRUE.

[START] wait for 1000 ms.
[DONE] wait for 1000 ms.

[START] intent request 'intent0' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"launch"},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.44e3c514-1b63-4c1d-8b66-31ceed01439a","timestamp":"2010-09-30T11:11:12Z","locale":"de-DE","intent":{"name":"intent0"}}}'.
[DONE] intent request 'intent0' in 22 ms.
->[TRUE] HasCard is TRUE.
->[TRUE] SessionEnded is NOT true.
->[TRUE] OutputSpeechPlainText does NOT exist.
->[TRUE] OutputSpeechPlainText is NOT equal to 'test'.
->[TRUE] OutputSpeechSsml matches pattern '.*test.*'.
->[TRUE] OutputSpeechSsml exists.

[START] intent request 'intent1' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"intent0"},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.5da93dd1-b3f7-4e6c-a2a6-b4d595792002","timestamp":"2010-09-30T11:11:12Z","locale":"de-DE","intent":{"name":"intent1","slots":{"slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}}}}}'.
[DONE] intent request 'intent1' in 3 ms.
->[TRUE] Session state with key 'slot1' is NOT null.
->[TRUE] Session state with key 'slot1' is equal to 'val'.
->[TRUE] Session state with key 'slot2' is NOT null.
->[TRUE] Session state with key 'slot2' is equal to '123'.
->[TRUE] Session state with key 'myDebugFlag' is NOT null.
->[TRUE] Session state with key 'myDebugFlag' is equal to 'true'.

[START] intent request 'AMAZON.YesIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"intent1","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.7c647d76-54ed-418e-a9d9-81284584b7b8","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.YesIntent"}}}'.
[DONE] intent request 'AMAZON.YesIntent' in 5 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.YesIntent'.
->[TRUE] Session state with key 'slot1' is NOT null.
->[TRUE] Session state with key 'slot1' is equal to 'val'.
->[TRUE] Session state with key 'slot2' is NOT null.
->[TRUE] Session state with key 'slot2' is equal to '123'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.NoIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.YesIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.3cf57f5e-3a3a-4f93-9e8e-07774b74bc13","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.NoIntent"}}}'.
[DONE] intent request 'AMAZON.NoIntent' in 2 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.NoIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.RepeatIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.NoIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.07e77f5f-026b-45ea-abb4-bc161c4d9f05","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.RepeatIntent"}}}'.
[DONE] intent request 'AMAZON.RepeatIntent' in 1 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.RepeatIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.CancelIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.RepeatIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.1b4a2e4f-54cd-467e-8c7f-1d26721a3e3b","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.CancelIntent"}}}'.
[DONE] intent request 'AMAZON.CancelIntent' in 7 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.CancelIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.StopIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.CancelIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.bd72143a-7858-458a-a216-a0fdd48968f5","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.StopIntent"}}}'.
[DONE] intent request 'AMAZON.StopIntent' in 1 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.StopIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.StartOverIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.StopIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.a38afcfd-d122-4ac3-8aa0-8d06008b52d5","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.StartOverIntent"}}}'.
[DONE] intent request 'AMAZON.StartOverIntent' in 1 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.StartOverIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] intent request 'AMAZON.HelpIntent' ...
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.StartOverIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"IntentRequest","requestId":"EdwRequestId.bb64e3cf-b617-477d-a127-635d425faa40","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","intent":{"name":"AMAZON.HelpIntent"}}}'.
[DONE] intent request 'AMAZON.HelpIntent' in 1 ms.
->[TRUE] Session state with key 'intent' is NOT null.
->[TRUE] Session state with key 'intent' is equal to 'AMAZON.HelpIntent'.
->[TRUE] SessionStillOpen is TRUE.

[START] request session end with reason 'USER_INITIATED'.
->[INFO] Invoke lambda function 'myLambdaFunctionName'.
->[INFO] with request payload '{"version":"1.0","session":{"new":false,"sessionId":"SessionId.567f262e-de60-4432-90bf-dcd78992db86","application":{"applicationId":"myApplicationId"},"attributes":{"myDebugFlag":true,"intent":"AMAZON.HelpIntent","slot1":{"name":"slot1","value":"val"},"slot2":{"name":"slot2","value":"123"}},"user":{"userId":"myUserId","accessToken":"myAccessToken"}},"context":null,"request":{"type":"SessionEndedRequest","requestId":"EdwRequestId.92f4cc2d-de9d-479e-a8bb-57423097f045","timestamp":"2010-09-30T11:11:13Z","locale":"de-DE","reason":"USER_INITIATED","error":null}}'.
[DONE] request session end with reason 'USER_INITIATED'.
```
