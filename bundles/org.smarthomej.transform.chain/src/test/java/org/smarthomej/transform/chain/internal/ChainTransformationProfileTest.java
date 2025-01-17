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
package org.smarthomej.transform.chain.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.smarthomej.transform.chain.internal.test.TestValueTransformationProvider;

/**
 * The {@link ChainTransformationProfileTest} are test cases for the ChainTransformationProfile
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class ChainTransformationProfileTest {
    public static final StringType INPUT_VALUE = new StringType("start");

    public static final String VALID_TRANSFORMATION_PATTERN = "APPEND:foo∩APPEND:bar∩DUPLICATE:noparam";
    public static final StringType EXPECTED_VALID_RESULT = new StringType("startfoobarstartfoobar");

    public static final String FAIL_TRANSFORMATION_PATTERN = "APPEND:foo∩FAIL:noparam∩DUPLICATE:noparam";

    @Mock
    private @NonNullByDefault({}) ProfileCallback callback;

    @Mock
    private @NonNullByDefault({}) ProfileContext context;

    private @NonNullByDefault({}) ArgumentCaptor<State> stateCaptor;
    private @NonNullByDefault({}) ArgumentCaptor<Command> commandCaptor;

    @BeforeEach
    public void setup() {
        stateCaptor = ArgumentCaptor.forClass(State.class);
        commandCaptor = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void validChainToItemTest() {
        StateProfile profile = getProfile(VALID_TRANSFORMATION_PATTERN, "", false);

        // state update from handler
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        assertSendUpdate(EXPECTED_VALID_RESULT);

        // command from handler
        profile.onCommandFromHandler(INPUT_VALUE);
        assertSendCommand(EXPECTED_VALID_RESULT);

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item - no transformation set, expected is input
        profile.onCommandFromItem(INPUT_VALUE);
        assertHandleCommand(INPUT_VALUE);
    }

    @Test
    public void validChainToChannelTest() {
        StateProfile profile = getProfile("", VALID_TRANSFORMATION_PATTERN, false);

        // state update from handler - no transformation set, expected is input
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        assertSendUpdate(INPUT_VALUE);

        // command from handler - no transformation set, expected is input
        profile.onCommandFromHandler(INPUT_VALUE);
        assertSendCommand(INPUT_VALUE);

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item
        profile.onCommandFromItem(INPUT_VALUE);
        assertHandleCommand(EXPECTED_VALID_RESULT);
    }

    @Test
    public void failChainToChannelTestDefault() {
        StateProfile profile = getProfile("", FAIL_TRANSFORMATION_PATTERN, false);

        // state update from handler - no transformation set, expected is input
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        assertSendUpdate(INPUT_VALUE);

        // command from handler - no transformation set, expected is input
        profile.onCommandFromHandler(INPUT_VALUE);
        assertSendCommand(INPUT_VALUE);

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item - fail ignored
        profile.onCommandFromItem(INPUT_VALUE);
        assertHandleCommand(null);
    }

    @Test
    public void failChainToItemTestDefault() {
        StateProfile profile = getProfile(FAIL_TRANSFORMATION_PATTERN, "", false);

        // state update from handler - fail ignored
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from handler - fail ignored
        profile.onCommandFromHandler(INPUT_VALUE);
        assertSendCommand(null);

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item - no transformation set, expected is input
        profile.onCommandFromItem(INPUT_VALUE);
        assertHandleCommand(INPUT_VALUE);
    }

    @Test
    public void failChainToChannelTestUndef() {
        StateProfile profile = getProfile("", FAIL_TRANSFORMATION_PATTERN, true);

        // state update from handler - no transformation set, expected is input
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        assertSendUpdate(INPUT_VALUE);

        // command from handler - no transformation set, expected is input
        profile.onCommandFromHandler(INPUT_VALUE);
        assertSendCommand(INPUT_VALUE);

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item - fail ignored
        profile.onCommandFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void failChainToItemTestUndef() {
        StateProfile profile = getProfile(FAIL_TRANSFORMATION_PATTERN, "", true);

        // state update from handler - fail results in UNDEF
        profile.onStateUpdateFromHandler(INPUT_VALUE);
        verify(callback).sendUpdate(eq(UnDefType.UNDEF));

        // command from handler - fail results in STATE update UNDEF
        profile.onCommandFromHandler(INPUT_VALUE);
        verify(callback, times(2)).sendUpdate(eq(UnDefType.UNDEF));

        // state update from item - should be ignored
        profile.onStateUpdateFromItem(INPUT_VALUE);
        verifyNoMoreInteractions(callback);

        // command from item - no transformation set, expected is input
        profile.onCommandFromItem(INPUT_VALUE);
        verify(callback).handleCommand(commandCaptor.capture());
        assertEquals(INPUT_VALUE, commandCaptor.getValue());
    }

    private void assertHandleCommand(@Nullable State expectedState) {
        if (expectedState == null) {
            verifyNoMoreInteractions(callback);
        } else {
            verify(callback).handleCommand(commandCaptor.capture());
            assertEquals(expectedState, commandCaptor.getValue());
        }
    }

    private void assertSendCommand(@Nullable State expectedState) {
        if (expectedState == null) {
            verifyNoMoreInteractions(callback);
        } else {
            verify(callback).sendCommand(commandCaptor.capture());
            assertEquals(expectedState, commandCaptor.getValue());
        }
    }

    private void assertSendUpdate(@Nullable State expectedState) {
        if (expectedState == null) {
            verifyNoMoreInteractions(callback);
        } else {
            verify(callback).sendUpdate(stateCaptor.capture());
            assertEquals(expectedState, stateCaptor.getValue());
        }
    }

    private StateProfile getProfile(String toItem, String toChannel, boolean undefOnError) {
        Map<String, Object> configuration = Map.of("toItem", toItem, "toChannel", toChannel, "undefOnError",
                undefOnError);
        doReturn(new Configuration(configuration)).when(context).getConfiguration();
        return new ChainTransformationProfile(callback, context, new TestValueTransformationProvider());
    }
}
