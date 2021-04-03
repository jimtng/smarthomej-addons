/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * Copyright (c) 2021 Contributors to the SmartHome/J project
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
package org.smarthomej.binding.dmx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ActionState} gives the state of an action
 *
 * waiting : not started yet
 * running : action is running
 * completed : action has completed, proceed to next action
 * completedfinal : action has completed, hold here
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public enum ActionState {
    WAITING,
    RUNNING,
    COMPLETED,
    COMPLETEDFINAL
}
