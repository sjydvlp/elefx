package com.sjydvlp.elefx.component.messagebox;

/** Immutable result delivered when a message box closes. */
public record EleFXMessageBoxResult(EleFXMessageBoxAction action, String value) {

    public boolean confirmed() {
        return action == EleFXMessageBoxAction.CONFIRM;
    }
}
