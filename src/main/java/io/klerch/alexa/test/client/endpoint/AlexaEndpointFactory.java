package io.klerch.alexa.test.client.endpoint;

import org.apache.log4j.Logger;
import org.joox.Match;

import java.lang.reflect.InvocationTargetException;

public class AlexaEndpointFactory {
    private final static Logger log = Logger.getLogger(AlexaEndpointFactory.class);

    public static AlexaEndpoint createEndpoint(final Match mEndpoint) {
        final String endpointType = mEndpoint.attr("type");
        final String endpointRef = mEndpoint.text();
        final String className = String.format("%1$s.Alexa%2$sEndpoint", AlexaEndpointFactory.class.getPackage().getName(), endpointType);

        try {
            // call static create method of endpoint class and receive builder instance
            final Object endpointBuilder = Class.forName(className)
                    .getDeclaredMethod("create", String.class)
                    .invoke(null, endpointRef);
            // call build method to get the endpoint class
            return (AlexaEndpoint)endpointBuilder.getClass()
                    .getMethod("build")
                    .invoke(endpointBuilder);
        } catch (final IllegalAccessException | InvocationTargetException| NoSuchMethodException | ClassNotFoundException e) {
            final String msg = String.format("Could not create endpoint of type %1$s with reference %2$s. %3$s caused an error: %4$s", endpointType, endpointRef, className, e.getMessage());
            log.error(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
