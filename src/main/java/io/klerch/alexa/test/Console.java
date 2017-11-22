package io.klerch.alexa.test;

import io.klerch.alexa.test.client.AlexaClient;
import org.apache.commons.lang3.Validate;

import java.io.*;

public class Console {
    public static void main(final String[] args){
        readFile(args);
    }

    private static void readFile(final String[] args) {
        Validate.notEmpty(args, "No script file path provided. Use -f followed by the file path of your YAML script file.");
        Validate.isTrue(args.length > 1, "Missing file path. Use -f followed by the file path of your YAML script file.");
        Validate.isTrue(args[0].equalsIgnoreCase("-f") || args[0].equalsIgnoreCase("-file"), "Missing -f or -file parameter. Use -f followed by the file path of your YAML script file." );

        try {
            final FileInputStream inputStream = new FileInputStream(args[1]);
            AlexaClient.create(inputStream).build().startScript();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
