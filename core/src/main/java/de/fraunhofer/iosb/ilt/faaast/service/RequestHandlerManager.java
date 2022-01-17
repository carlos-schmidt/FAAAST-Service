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
package de.fraunhofer.iosb.ilt.faaast.service;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds available RequestHandlers and handles execution (sync or async)
 */
public class RequestHandlerManager {
    private static Logger logger = LoggerFactory.getLogger(RequestHandlerManager.class);
    private Map<Class<? extends Request>, ? extends RequestHandler> handlers;
    private ExecutorService requestHandlerExecutorService;
    private final CoreConfig coreConfig;
    private final Persistence persistence;
    private final MessageBus messageBus;
    private final AssetConnectionManager assetConnectionManager;

    public RequestHandlerManager(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        this.coreConfig = coreConfig;
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
        init();
    }


    private void init() {
        // TODO implement build-time scan to improve performance (see https://github.com/classgraph/classgraph/wiki/Build-Time-Scanning)
        final Object[] constructorArgs = new Object[] {
                persistence,
                messageBus,
                assetConnectionManager
        };
        final Class<?>[] constructorArgTypes = RequestHandler.class.getConstructors()[0].getParameterTypes();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            // TODO change approach for RequestHandler from abstract class to interface 
            // (either with init method or pass all arguments with handle method)
            handlers = scanResult.getSubclasses(RequestHandler.class).loadClasses().stream()
                    .map(x -> (Class<? extends RequestHandler>) x)
                    .collect(Collectors.toMap(
                            x -> {
                                return (Class<? extends Request>) TypeToken.of(x).resolveType(RequestHandler.class.getTypeParameters()[0]).getRawType();
                            },
                            x -> {
                                try {
                                    Constructor<? extends RequestHandler> constructor = x.getConstructor(constructorArgTypes);
                                    return constructor.newInstance(constructorArgs);
                                }
                                catch (NoSuchMethodException | SecurityException ex) {
                                    logger.warn("request handler implementation could not be loaded, "
                                            + "reason: missing constructor (implementation class: {}, required constructor signature: {}",
                                            x.getName(),
                                            constructorArgTypes);
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    logger.warn("request handler implementation could not be loaded, "
                                            + "reason: calling constructor failed (implementation class: {}, constructor arguments: {}",
                                            x.getName(),
                                            constructorArgs);
                                }
                                return null;
                            }));
        }
        // filter out null values from handlers that could not be instantiated so that later we don't need to check for null on each access

        // create request handler executor service 
        requestHandlerExecutorService = Executors.newFixedThreadPool(
                coreConfig.getRequestHandlerThreadPoolSize(),
                new BasicThreadFactory.Builder()
                        .namingPattern("RequestHandler" + "-%d")
                        .build());
    }


    /**
     * Properly shuts down this instance and releases all resources. Do not call any methods on this instance after calling
     * this method.
     */
    public void shutdown() {
        requestHandlerExecutorService.shutdown();
        try {
            if (requestHandlerExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                return;
            }
        }
        catch (InterruptedException ex) {
            logger.error("Interrupted while waiting for shutdown.", ex);
            Thread.currentThread().interrupt();
        }
        logger.warn("RequestHandlerManager stopped with {} unfinished requests.",
                requestHandlerExecutorService.shutdownNow().size());
    }


    /**
     * Executes a request synchroniously.
     * 
     * @param <I> type of request/input
     * @param <O> type of response/output
     * @param request the request to execute
     * @return the reponse to this request
     */
    public <I extends Request<O>, O extends Response> O execute(I request) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        if (!handlers.containsKey(request.getClass())) {
            // TODO throwing exceptions vs returning response, probably throwing is better here
            throw new RuntimeException("no handler defined for this request");
        }
        return (O) handlers.get(request.getClass()).process(request);
    }


    /**
     * Executes a request asynchroniously.
     * 
     * @param <I> type of request/input
     * @param <O> type of response/output
     * @param request the request to execute
     * @param callback callback handler which is called with the response once the request has been executed
     */
    public <I extends Request<O>, O extends Response> void executeAsync(I request, Consumer<O> callback) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must be non-null");
        }
        requestHandlerExecutorService.submit(() -> {
            callback.accept(execute(request));
        });
    }
}