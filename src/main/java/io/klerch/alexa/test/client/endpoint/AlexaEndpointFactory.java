package io.klerch.alexa.test.client.endpoint;

import org.apache.log4j.Logger;
import org.joox.Match;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Optional;

public class AlexaEndpointFactory {
    private final static Logger log = Logger.getLogger(AlexaEndpointFactory.class);

    public static AlexaEndpoint createEndpoint(final HashMap<Object, Object> endpointConfiguration) {
        final String endpointType = Optional.ofNullable(endpointConfiguration.get("type")).filter(o -> o instanceof String).map(Object::toString).orElseThrow(() -> new RuntimeException("Type in Configuration must contain a valid String value."));
        final String className = String.format("%1$s.Alexa%2$sEndpoint", AlexaEndpointFactory.class.getPackage().getName(), endpointType);

        try {
            // call static create method of endpoint class and receive builder instance
            final Object endpointBuilder = Class.forName(className)
                    .getDeclaredMethod("create", HashMap.class)
                    .invoke(null, endpointConfiguration);
            // call build method to get the endpoint class
            return (AlexaEndpoint)endpointBuilder.getClass()
                    .getMethod("build")
                    .invoke(endpointBuilder);
        } catch (final IllegalAccessException | InvocationTargetException| NoSuchMethodException | ClassNotFoundException e) {
            final String msg = String.format("Could not create endpoint of type %1$s. %2$s caused an error: %3$s", endpointType, className, e.getMessage());
            log.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    public static AlexaEndpoint createEndpoint(final Match mEndpoint) {
        final String endpointType = Optional.ofNullable(mEndpoint.attr("type")).orElseThrow(() -> new RuntimeException("Endpoint configuration is missing the type-attribute."));
        final String endpointRef = Optional.ofNullable(mEndpoint.text()).orElseThrow(() -> new RuntimeException("Endpoint configuration is missing the endpoint reference string."));
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
