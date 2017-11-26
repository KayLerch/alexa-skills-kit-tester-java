package io.klerch.alexa.test;

import io.klerch.alexa.test.client.AlexaClient;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.io.*;

public class Console {
    private final static String errNoFilePath = "[ERROR] Missing file path. Use -f followed by the file path of your YAML script file.";
    private final static Logger log = Logger.getLogger(Console.class);

    public static void main(final String[] args){
        readFile(args);
    }

    private static void readFile(final String[] args) {
        Validate.notEmpty(args, errNoFilePath);
        Validate.isTrue(args.length > 1, errNoFilePath);
        Validate.isTrue(args[0].equalsIgnoreCase("-f") || args[0].equalsIgnoreCase("-file"), "Missing -f or -file parameter. Use -f followed by the file path of your YAML script file." );

        try {
            AlexaClient.create(args[1]).build().startScript();
        } catch (final IOException e) {
            log.error(e);
            e.printStackTrace();
        }
    }
}
