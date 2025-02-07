/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.starter;

import static de.fraunhofer.iosb.ilt.faaast.service.starter.App.APP_NAME;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ValueTypeValidator;
import de.fraunhofer.iosb.ilt.faaast.service.starter.cli.LogLevelTypeConverter;
import de.fraunhofer.iosb.ilt.faaast.service.starter.logging.FaaastFilter;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ServiceConfigHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ImplementationManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.validator.ShaclValidator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;


/**
 * Class for configuring and starting a FA³ST Service.
 */
@Command(name = APP_NAME, mixinStandardHelpOptions = true, description = "Starts a FA³ST Service", versionProvider = App.PropertiesVersionProvider.class, usageHelpAutoWidth = true)
public class App implements Runnable {

    protected static final String APP_NAME = "FA³ST Service Starter";
    private static final int INDENT_DEFAULT = 20;
    private static final int INDENT_STEP = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final CountDownLatch SHUTDOWN_FINISHED = new CountDownLatch(1);
    private static final CountDownLatch SHUTDOWN_REQUESTED = new CountDownLatch(1);
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static AtomicReference<Service> serviceRef = new AtomicReference<>();
    // commands
    protected static final String COMMAND_CONFIG = "--config";
    protected static final String COMMAND_MODEL = "--model";
    // config
    protected static final String CONFIG_FILENAME_DEFAULT = "config.json";
    protected static final String ENV_CONFIG_KEY = "config";
    protected static final String ENV_EXTENSION_KEY = "extension";
    protected static final String ENV_FAAAST_KEY = "faaast";
    protected static final String ENV_CONFIG_FILE_PATH = envPath(ENV_FAAAST_KEY, ENV_CONFIG_KEY);

    protected static final String ENV_CONFIG_EXTENSION_PREFIX = envPath(ENV_FAAAST_KEY, ENV_CONFIG_KEY, ENV_EXTENSION_KEY, "");
    protected static final String ENV_MODEL_KEY = "model";
    protected static final String ENV_MODEL_FILE_PATH = envPath(ENV_FAAAST_KEY, ENV_MODEL_KEY);
    // environment
    protected static final String ENV_PATH_SEPERATOR = ".";
    // model
    protected static final String MODEL_FILENAME_DEFAULT = "aasenvironment.*";
    protected static final String MODEL_FILENAME_PATTERN = "aasenvironment\\..*";

    @Option(names = "--no-autoCompleteConfig", negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public boolean autoCompleteConfiguration = true;

    @Option(names = {
            "-c",
            COMMAND_CONFIG
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = CONFIG_FILENAME_DEFAULT)
    public File configFile;

    @Option(names = "--endpoint", split = ",", description = "Additional endpoints that should be started.")
    public List<EndpointType> endpoints = new ArrayList<>();
    @Option(names = {
            "-m",
            COMMAND_MODEL
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = MODEL_FILENAME_DEFAULT)
    public File modelFile;

    @Parameters(description = "Additional properties to override values of configuration using JSONPath notation without starting '$.' (see https://goessner.net/articles/JsonPath/)")
    public Map<String, String> properties = new HashMap<>();

    @Option(names = "--emptyModel", description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public boolean useEmptyModel = false;

    @Option(names = "--no-modelValidation", negatable = true, description = "Validates the AAS Environment. True by default")
    public boolean validateModel = true;

    @Option(names = "--loglevel-faaast", description = "Sets the log level for FA³ST packages. This overrides the log level defined by other commands such as -q or -v.")
    public Level logLevelFaaast;

    @Option(names = "--loglevel-external", description = "Sets the log level for external packages. This overrides the log level defined by other commands such as -q or -v.")
    public Level logLevelExternal;

    @Option(names = {
            "-q",
            "--quite"
    }, description = "Reduces log output (ERROR for FA³ST packages, ERROR for all other packages). Default information about the starting process will still be printed.")
    public boolean quite = false;

    @Option(names = {
            "-v",
            "--verbose"
    }, description = "Enables verbose logging (INFO for FA³ST packages, WARN for all other packages).")
    public boolean verbose = false;

    @Option(names = "-vv", description = "Enables very verbose logging (DEBUG for FA³ST packages, INFO for all other packages).")
    public boolean veryVerbose = false;

    @Option(names = "-vvv", description = "Enables very very verbose logging (TRACE for FA³ST packages, DEBUG for all other packages).")
    public boolean veryVeryVerbose = false;

    protected boolean dryRun = false;

    @Spec
    private CommandSpec spec;
    private static int exitCode = -1;

    /**
     * Main entry point.
     *
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (exitCode == CommandLine.ExitCode.OK) {
                    SHUTDOWN_REQUESTED.countDown();
                    try {
                        SHUTDOWN_FINISHED.await();
                    }
                    catch (InterruptedException ex) {
                        LOGGER.error("Error while waiting for FA³ST Service to gracefully shutdown");
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        LOGGER.info("Goodbye!");
                    }
                }
            }
        });
        CommandLine commandLine = new CommandLine(new App())
                .registerConverter(Level.class, new LogLevelTypeConverter())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);
        try {
            CommandLine.ParseResult result = commandLine.parseArgs(args);
            if (result.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                return;
            }
            else if (result.isVersionHelpRequested()) {
                commandLine.printVersionHelp(System.out);
                return;
            }
        }
        catch (CommandLine.ParameterException e) {
            // intentionally left empty
        }
        exitCode = commandLine.execute(args);
        if (exitCode == CommandLine.ExitCode.OK) {
            try {
                SHUTDOWN_REQUESTED.await();
            }
            catch (InterruptedException e) {
                LOGGER.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
            finally {
                LOGGER.info("Shutting down FA³ST Service...");
                if (serviceRef.get() != null) {
                    serviceRef.get().stop();
                }
                LOGGER.info("FA³ST Service successfully shut down");
                SHUTDOWN_FINISHED.countDown();
            }
        }
        else {
            System.exit(exitCode);
        }
    }


    private static String envPath(String... args) {
        return Stream.of(args).collect(Collectors.joining(ENV_PATH_SEPERATOR));
    }


    private static String indent(String value, int steps) {
        return String.format(String.format("%%%ds%s", INDENT_DEFAULT + (INDENT_STEP * steps), value), "");
    }


    private void validateModelIfRequired(ServiceConfig config) {
        if (validateModel) {
            try {
                AssetAdministrationShellEnvironment model = config.getPersistence().getInitialModel() == null
                        ? EnvironmentSerializationManager.deserialize(config.getPersistence().getInitialModelFile()).getEnvironment()
                        : config.getPersistence().getInitialModel();
                validate(model);
            }
            catch (IOException e) {
                throw new InitializationException("Unexpected exception while validating model", e);
            }
            catch (DeserializationException e) {
                throw new InitializationException("Error loading model file", e);
            }
        }
    }


    private void configureLogging() {
        if (veryVeryVerbose) {
            FaaastFilter.setLevelFaaast(Level.TRACE);
            FaaastFilter.setLevelExternal(Level.DEBUG);
        }
        else if (veryVerbose) {
            FaaastFilter.setLevelFaaast(Level.DEBUG);
            FaaastFilter.setLevelExternal(Level.INFO);
        }
        else if (verbose) {
            FaaastFilter.setLevelFaaast(Level.INFO);
            FaaastFilter.setLevelExternal(Level.WARN);
        }
        else if (quite) {
            FaaastFilter.setLevelFaaast(Level.ERROR);
            FaaastFilter.setLevelExternal(Level.ERROR);
        }
        if (logLevelFaaast != null) {
            FaaastFilter.setLevelFaaast(logLevelFaaast);
        }
        if (logLevelExternal != null) {
            FaaastFilter.setLevelExternal(logLevelExternal);
        }
        LOGGER.info("Using log level for FA³ST packages: {}", FaaastFilter.getLevelFaaast());
        LOGGER.info("Using log level for external packages: {}", FaaastFilter.getLevelExternal());
    }


    @Override
    public void run() {
        configureLogging();
        printHeader();
        ImplementationManager.init();
        ServiceConfig config = null;
        try {
            config = getConfig();
        }
        catch (IOException e) {
            throw new InitializationException("Error loading config file", e);
        }
        if (autoCompleteConfiguration) {
            ServiceConfigHelper.autoComplete(config);
        }
        withModel(config);
        validateModelIfRequired(config);
        try {
            ServiceConfigHelper.apply(config, endpoints.stream()
                    .map(LambdaExceptionHelper.rethrowFunction(
                            x -> x.getImplementation().getDeclaredConstructor().newInstance()))
                    .collect(Collectors.toList()));
        }
        catch (InvalidConfigurationException | ReflectiveOperationException e) {
            throw new InitializationException("Adding endpoints to config failed", e);
        }
        try {
            config = ServiceConfigHelper.withProperties(config, getConfigOverrides());
        }
        catch (JsonProcessingException e) {
            throw new InitializationException("Overriding config properties failed", e);
        }
        if (!dryRun) {
            runService(config);
        }
    }


    private void runService(ServiceConfig config) {
        try {
            serviceRef.set(new Service(config));
            LOGGER.info("Starting FA³ST Service...");
            LOGGER.debug("Using configuration file: ");
            printConfig(config);
            serviceRef.get().start();
            LOGGER.info("FA³ST Service successfully started");
            printEndpointInfo(config);
            LOGGER.info("Press CTRL + C to stop");
        }
        catch (Exception e) {
            LOGGER.error("Unexpected exception encountered while executing FA³ST Service", e);
        }
    }


    private Optional<File> findDefaultModel() {
        try {
            List<File> modelFiles;
            try (Stream<File> stream = Files.find(Paths.get(""), 1,
                    (file, attributes) -> file.toFile()
                            .getName()
                            .matches(MODEL_FILENAME_PATTERN))
                    .map(Path::toFile)) {
                modelFiles = stream.collect(Collectors.toList());
            }
            if (modelFiles.size() > 1 && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Found multiple model files matching the default pattern. To use a specific one use command '{} <filename>' (files found: {}, file pattern: {})",
                        COMMAND_MODEL,
                        modelFiles.stream()
                                .map(File::getName)
                                .collect(Collectors.joining(",", "[", "]")),
                        MODEL_FILENAME_PATTERN);
            }
            return modelFiles.stream().findFirst();
        }
        catch (IOException ex) {
            return Optional.empty();
        }
    }


    private ServiceConfig getConfig() throws IOException {
        if (spec.commandLine().getParseResult().hasMatchedOption(COMMAND_CONFIG)) {
            try {
                LOGGER.info("Config: {} (CLI)", configFile.getCanonicalFile());
            }
            catch (IOException e) {
                LOGGER.info("Retrieving path of config file failed with {}", e.getMessage());
            }
            return ServiceConfigHelper.load(configFile);
        }
        if (System.getenv(ENV_CONFIG_FILE_PATH) != null && !System.getenv(ENV_CONFIG_FILE_PATH).isBlank()) {
            LOGGER.info("Config: {} (ENV)", System.getenv(ENV_CONFIG_FILE_PATH));
            configFile = new File(System.getenv(ENV_CONFIG_FILE_PATH));
            return ServiceConfigHelper.load(new File(System.getenv(ENV_CONFIG_FILE_PATH)));
        }
        if (new File(CONFIG_FILENAME_DEFAULT).exists()) {
            configFile = new File(CONFIG_FILENAME_DEFAULT);
            try {
                LOGGER.info("Config: {} (default location)", configFile.getCanonicalFile());
            }
            catch (IOException e) {
                LOGGER.info("Retrieving path of config file failed with {}", e.getMessage());
            }
            return ServiceConfigHelper.load(configFile);
        }
        LOGGER.info("Config: empty (default)");
        return ServiceConfigHelper.getDefaultServiceConfig();
    }


    private void withModel(ServiceConfig config) {
        if (spec.commandLine().getParseResult().hasMatchedOption(COMMAND_MODEL)) {
            try {
                LOGGER.info("Model: {} (CLI)", modelFile.getCanonicalFile());
                if (config.getPersistence().getInitialModelFile() != null) {
                    LOGGER.info("Overriding Model Path {} set in Config File with {}",
                            config.getPersistence().getInitialModelFile(),
                            modelFile.getCanonicalFile());
                }
            }
            catch (IOException e) {
                LOGGER.info("Retrieving path of model file failed with {}", e.getMessage());
            }
            config.getPersistence().setInitialModelFile(modelFile);
            return;

        }
        if (System.getenv(ENV_MODEL_FILE_PATH) != null && !System.getenv(ENV_MODEL_FILE_PATH).isBlank()) {
            LOGGER.info("Model: {} (ENV)", System.getenv(ENV_MODEL_FILE_PATH));
            if (config.getPersistence().getInitialModelFile() != null) {
                LOGGER.info("Overriding model path {} set in Config File with {}",
                        config.getPersistence().getInitialModelFile(),
                        System.getenv(ENV_MODEL_FILE_PATH));
            }
            config.getPersistence().setInitialModelFile(new File(System.getenv(ENV_MODEL_FILE_PATH)));
            modelFile = new File(System.getenv(ENV_MODEL_FILE_PATH));
            return;
        }

        if (config.getPersistence().getInitialModelFile() != null) {
            LOGGER.info("Model: {} (CONFIG)", config.getPersistence().getInitialModelFile());
            return;
        }

        Optional<File> defaultModel = findDefaultModel();
        if (defaultModel.isPresent()) {
            LOGGER.info("Model: {} (default location)", defaultModel.get().getAbsoluteFile());
            config.getPersistence().setInitialModelFile(defaultModel.get());
            modelFile = new File(defaultModel.get().getAbsolutePath());
            return;
        }
        if (useEmptyModel) {
            LOGGER.info("Model: empty (CLI)");
        }
        else {
            LOGGER.info("Model: empty (default)");
        }
        LOGGER.info("Model validation is disabled when using empty model");
        validateModel = false;
        config.getPersistence().setInitialModel(new DefaultAssetAdministrationShellEnvironment.Builder().build());
    }


    private void printConfig(ServiceConfig config) {
        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(mapper.writeValueAsString(config));
            }
            catch (JsonProcessingException e) {
                LOGGER.debug("Printing config failed", e);
            }
        }
    }


    private void printEndpointInfo(ServiceConfig config) {
        if (LOGGER.isInfoEnabled()) {
            config.getEndpoints().stream().forEach(x -> {
                if (HttpEndpointConfig.class.isAssignableFrom(x.getClass())) {
                    LOGGER.info("HTTP endpoint available on port {}", ((HttpEndpointConfig) x).getPort());
                }
                else if (OpcUaEndpointConfig.class.isAssignableFrom(x.getClass())) {
                    LOGGER.info("OPC UA endpoint available on port {}", ((OpcUaEndpointConfig) x).getTcpPort());
                }
            });
        }
    }


    private void printHeader() {
        LOGGER.info("            _____                                                       ");
        LOGGER.info("           |___ /                                                       ");
        LOGGER.info(" ______      |_ \\    _____ _______     _____                 _          ");
        LOGGER.info("|  ____/\\   ___) | / ____|__   __|    / ____|               (_)         ");
        LOGGER.info("| |__ /  \\ |____/ | (___    | |      | (___   ___ _ ____   ___  ___ ___ ");
        LOGGER.info("|  __/ /\\ \\        \\___ \\   | |       \\___ \\ / _ \\ '__\\ \\ / / |/ __/ _ \\");
        LOGGER.info("| | / ____ \\       ____) |  | |       ____) |  __/ |   \\ V /| | (_|  __/");
        LOGGER.info("|_|/_/    \\_\\     |_____/   |_|      |_____/ \\___|_|    \\_/ |_|\\___\\___|");
        LOGGER.info("");
        LOGGER.info("-------------------------------------------------------------------------");
        try {
            Stream.of(new PropertiesVersionProvider().getVersion()).forEach(LOGGER::info);
        }
        catch (Exception e) {
            LOGGER.info("error determining version info (reason: {})", e.getMessage());
        }
        LOGGER.info("-------------------------------------------------------------------------");
    }


    private void validate(AssetAdministrationShellEnvironment aasEnv) throws IOException {
        LOGGER.debug("Validating model...");
        try {
            ValueTypeValidator.validate(aasEnv);
        }
        catch (ValidationException e) {
            throw new InitializationException(
                    String.format("Model type validation failed with the following error(s):%s%s",
                            System.lineSeparator(),
                            e.getMessage()));
        }
        ShaclValidator shaclValidator = ShaclValidator.getInstance();
        ValidationReport report = shaclValidator.validateGetReport(aasEnv);
        if (!report.conforms()) {
            ByteArrayOutputStream validationResultStream = new ByteArrayOutputStream();
            ShLib.printReport(validationResultStream, report);
            throw new InitializationException(
                    String.format("Detailed model validation failed with the following error(s):%s%s",
                            System.lineSeparator(),
                            validationResultStream));
        }
        LOGGER.info("Model successfully validated");
    }


    /**
     * Collects config overrides from environment and CLI parameters.
     *
     * @return map of config overrides
     */
    protected Map<String, String> getConfigOverrides() {
        Map<String, String> envParameters = System.getenv().entrySet().stream()
                .filter(x -> x.getKey().startsWith(ENV_CONFIG_EXTENSION_PREFIX))
                .filter(x -> !properties.containsKey(x.getKey().substring(ENV_CONFIG_EXTENSION_PREFIX.length() - 1)))
                .collect(Collectors.toMap(
                        x -> x.getKey().substring(ENV_CONFIG_EXTENSION_PREFIX.length()),
                        Entry::getValue));
        Map<String, String> result = new HashMap<>(envParameters);
        for (var property: properties.entrySet()) {
            if (property.getKey().startsWith(ENV_CONFIG_EXTENSION_PREFIX)) {
                String realKey = property.getKey().substring(ENV_CONFIG_EXTENSION_PREFIX.length());
                LOGGER.info("Found unnecessary prefix for CLI parameter '{}' (remove prefix '{}' to not receive this message any longer)", realKey, ENV_CONFIG_EXTENSION_PREFIX);
                result.put(realKey, property.getValue());
            }
            else {
                result.put(property.getKey(), property.getValue());
            }
        }
        if (!result.isEmpty()) {
            LOGGER.info("Overriding config parameter: {}{}",
                    System.lineSeparator(),
                    result.entrySet().stream()
                            .map(x -> indent(
                                    String.format("%s=%s [%s]",
                                            x.getKey(),
                                            x.getValue(),
                                            properties.containsKey(x.getKey()) ? "CLI" : "ENV"),
                                    1))
                            .collect(Collectors.joining(System.lineSeparator())));
        }
        return result;
    }

    /**
     * Provides version information from properies.
     */
    protected static class PropertiesVersionProvider implements IVersionProvider {

        private static final String PATH_GIT_BUILD_VERSION = "git.build.version";
        private static final String PATH_GIT_COMMIT_TIME = "git.commit.time";
        private static final String PATH_GIT_COMMIT_ID_DESCRIBE = "git.commit.id.describe";
        private static final TypeReference<Map<String, String>> TYPE_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
            // Empty on purpose.
        };

        @Override
        public String[] getVersion() throws Exception {
            Map<String, String> gitInfo = new ObjectMapper().readValue(App.class.getClassLoader().getResourceAsStream("git.json"), TYPE_MAP_STRING_STRING);
            return new String[] {
                    String.format("Version %s", gitInfo.get(PATH_GIT_BUILD_VERSION)),
                    String.format("Git Commit ID: %s", gitInfo.get(PATH_GIT_COMMIT_ID_DESCRIBE)),
                    String.format("Commit Time: %s", gitInfo.get(PATH_GIT_COMMIT_TIME)),
            };
        }
    }
}
