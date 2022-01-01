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
package org.smarthomej.binding.tuya.internal.cloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ConnectionException} is thrown if a connection problem caused the request to fail
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ConnectionException extends Exception {
    static final long serialVersionUID = 1L;

    public ConnectionException(@Nullable String message) {
        super(message);
    }
}
