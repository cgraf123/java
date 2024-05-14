package org.cli;

import org.apache.commons.cli.*;
import org.http.ApiClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Client {

    // Host
    static final String HOST = "host";
    static final URI DEFAULT_URI;
    static {
        try {
            DEFAULT_URI = new URI("http://127.0.0.1:5000/api/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Commands
    static final String COMMAND = "command";
    static final String ADD = "add";
    static final String DELETE = "delete";
    static final String UPDATE = "update";
    static final String GET_UUID = "get_uuid";
    static final String GET_UUIDS = "get_uuids";
    static final List<String> COMMANDS = Arrays.asList(ADD, DELETE, UPDATE, GET_UUID, GET_UUIDS);

    // Command options
    static final String PATH_OPT = "file";
    static final String UUID_OPT = "uuid";

    // Other options
    static final String HELP_OPT = "help";

    public static void main(String[] args) throws ParseException {
        Option hostOption = new Option("t", HOST, false, "API target host, defaults to: " + DEFAULT_URI);
        Option commandOption = new Option("c", COMMAND, true, "client command, one of: [" + String.join(",", COMMANDS) + "]");
        commandOption.setRequired(true);
        Option fileOption = new Option("f", PATH_OPT, true, "GeoJSON file path");
        Option uuidOption = new Option("u", UUID_OPT, true, "target UUID");
        Option helpOption = new Option("h", HELP_OPT, false, "print help");
        Options options = new Options();
        options.addOption(hostOption);
        options.addOption(commandOption);
        options.addOption(fileOption);
        options.addOption(uuidOption);
        options.addOption(helpOption);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(helpOption)) {
                throw new MissingOptionException("help option");
            }
            String hostValue = cmd.getOptionValue(hostOption);
            String commandValue = cmd.getOptionValue(commandOption);
            String fileValue = cmd.getOptionValue(fileOption);
            String uuidValue = cmd.getOptionValue(uuidOption);
            final Path path = fileValue != null ? Paths.get(fileValue) : null;
            final UUID uuid = uuidValue != null ? UUID.fromString(uuidValue) : null;
            final URI uri = hostValue == null ? DEFAULT_URI : new URI(hostValue);
            final ApiClient apiClient = new ApiClient(uri, true);
            final String content;
            switch (commandValue) {
                case ADD:
                    content = apiClient.add(validate(path, PATH_OPT)).toString();
                    break;
                case DELETE:
                    content = apiClient.delete(validate(uuid, UUID_OPT)).toString();
                    break;
                case UPDATE:
                    content = apiClient.add(validate(path, PATH_OPT), validate(uuid, UUID_OPT)).toString();
                    break;
                case GET_UUID:
                    content = apiClient.get_uuid(validate(uuid, UUID_OPT));
                    break;
                case GET_UUIDS:
                    content = apiClient.get_uuids();
                    break;
                default:
                    throw new IllegalArgumentException("unhandled command: " + commandValue);
            }
            System.out.println(content);
        } catch (MissingOptionException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Client.class.getSimpleName(), options);
        } catch (URISyntaxException e) {
            System.err.println("invalid host URI value: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("API client connection error");
            throw new RuntimeException(e);
        }
    }

    private static <T> T validate(T object, String name) throws MissingOptionException {
        if (object == null) {
            throw new MissingOptionException("missing " + name + " value");
        }
        return object;
    }
}