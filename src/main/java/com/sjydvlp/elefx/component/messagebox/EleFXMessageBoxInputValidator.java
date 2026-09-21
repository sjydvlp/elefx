package com.sjydvlp.elefx.component.messagebox;

/**
 * Validates prompt input. Return {@code Boolean.TRUE} for success, {@code Boolean.FALSE}
 * for the configured error message, or a non-empty {@code String} for a custom error message.
 */
@FunctionalInterface
public interface EleFXMessageBoxInputValidator {

    Object validate(String value);
}
