/**
 * Copyright (c) 2021-2022 Contributors to the SmartHome/J project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.smarthomej.automation.javarule;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.binding.ThingActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingActionsWrapper} is a wrapper for the core ThingActions which is not exposed
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ThingActionsWrapper {
    private final Logger logger = LoggerFactory.getLogger(ThingActionsWrapper.class);

    private final ClassLoader classLoader;
    private final Object coreThingActions;

    public ThingActionsWrapper(Object coreThingActions, JavaRule javaRule) {
        this.coreThingActions = coreThingActions;
        ClassLoader memoryClassloader = Objects.requireNonNull(javaRule.getClass().getClassLoader());
        classLoader = Objects.requireNonNull(memoryClassloader.getParent());
    }

    /**
     * Get a {@link Proxy} object for the requested thing action
     *
     * This is necessary since at some point all {@link org.openhab.core.thing.binding.ThingActions} classes have been
     * moved to internal packages. The proxy interfaces are generated by the
     * {@link org.smarthomej.automation.javarule.internal.compiler.ClassGenerator} as soon as a thing that allows the
     * action is initialized.
     *
     * @param scope The scope of the requested actions class
     * @param thingUid The UID of the thing as string
     * @return {@link Object} that either is the {@link ThingActions} object, a proxy or null if not found
     */
    public @Nullable Object get(@Nullable String scope, @Nullable String thingUid) {
        if (scope == null || thingUid == null) {
            return null;
        }

        try {
            Method get = coreThingActions.getClass().getDeclaredMethod("get", String.class, String.class);
            Object thingAction = get.invoke(coreThingActions, scope, thingUid);
            if (thingAction == null) {
                return null;
            }
            String className = thingAction.getClass().getName();
            Class<?> clazz = classLoader.loadClass(className);

            if (clazz.isInterface()) {
                // if the class is an interface, we have to proxy it
                return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
                        new DynamicInvocationHandler(thingAction));
            } else {
                return thingAction;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                | IllegalAccessException e) {
            logger.warn("Could not create proxy object for ThingActions in scope '{}' thing '{}': {}", scope, thingUid,
                    e.getMessage());
        }

        return null;
    }

    private static class DynamicInvocationHandler implements InvocationHandler {
        private final Logger logger = LoggerFactory.getLogger(DynamicInvocationHandler.class);
        private final Object target;

        public DynamicInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public @Nullable Object invoke(Object proxy, Method method, @Nullable Object @Nullable [] args)
                throws Throwable {
            String methodName = method.getName();
            String methodSignature = method.toString().replace(" abstract", "");

            logger.trace("Invoked method: '{}' with args '{}'", methodName, args);

            Method[] methods = target.getClass().getDeclaredMethods();
            Optional<Method> targetMethod = Arrays.stream(methods).filter(m -> m.toString().equals(methodSignature))
                    .findFirst();

            if (targetMethod.isEmpty()) {
                logger.warn("Could not find method in target object. This is a bug.");
                return null;
            }

            return targetMethod.get().invoke(target, args);
        }
    }
}
