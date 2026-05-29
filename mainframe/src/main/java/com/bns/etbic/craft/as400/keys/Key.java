package com.bns.etbic.craft.as400.keys;

import org.tn5250j.keyboard.KeyMnemonic;

/**
 * Type-safe enumeration of the 5250 AID and editing keys the driver can send,
 * each mapped to its tn5250j {@link KeyMnemonic}.
 *
 * @author Andres Acosta
 * @since 0.1.0
 */
public enum Key {

    /** Enter / field-advance AID key. */
    ENTER(KeyMnemonic.ENTER),
    /** Advance to the next field. */
    TAB(KeyMnemonic.TAB),
    /** Move back to the previous field. */
    BACK_TAB(KeyMnemonic.BACK_TAB),
    /** Delete the character to the left of the cursor. */
    BACKSPACE(KeyMnemonic.BACK_SPACE),
    /** Delete the character under the cursor. */
    DELETE(KeyMnemonic.DELETE),
    /** Toggle insert mode. */
    INSERT(KeyMnemonic.INSERT),
    /** Move the cursor to the home position. */
    HOME(KeyMnemonic.HOME),
    /** Move the cursor to the end of the current field. */
    END_OF_FIELD(KeyMnemonic.END_OF_FIELD),
    /** Move the cursor to the start of the current field. */
    BEGIN_OF_FIELD(KeyMnemonic.BEGIN_OF_FIELD),
    /** Erase from the cursor to the end of the field. */
    ERASE_EOF(KeyMnemonic.ERASE_EOF),
    /** Erase the entire current field. */
    ERASE_FIELD(KeyMnemonic.ERASE_FIELD),
    /** Exit the current field (field exit). */
    FIELD_EXIT(KeyMnemonic.FIELD_EXIT),
    /** Field-exit marking the value as positive. */
    FIELD_PLUS(KeyMnemonic.FIELD_PLUS),
    /** Field-exit marking the value as negative. */
    FIELD_MINUS(KeyMnemonic.FIELD_MINUS),
    /** Advance to the first field of the next line. */
    NEW_LINE(KeyMnemonic.NEW_LINE),

    /** Move the cursor up one row. */
    UP(KeyMnemonic.UP),
    /** Move the cursor down one row. */
    DOWN(KeyMnemonic.DOWN),
    /** Move the cursor left one column. */
    LEFT(KeyMnemonic.LEFT),
    /** Move the cursor right one column. */
    RIGHT(KeyMnemonic.RIGHT),

    /** Clear the screen (Clear AID key). */
    CLEAR(KeyMnemonic.CLEAR),
    /** Help AID key. */
    HELP(KeyMnemonic.HELP),
    /** Page up / roll down. */
    PAGE_UP(KeyMnemonic.PAGE_UP),
    /** Page down / roll up. */
    PAGE_DOWN(KeyMnemonic.PAGE_DOWN),
    /** Roll the screen left. */
    ROLL_LEFT(KeyMnemonic.ROLL_LEFT),
    /** Roll the screen right. */
    ROLL_RIGHT(KeyMnemonic.ROLL_RIGHT),

    /** Reset the keyboard after an input-inhibited state. */
    RESET(KeyMnemonic.RESET),
    /** Attention key. */
    ATTN(KeyMnemonic.ATTN),
    /** System request key. */
    SYSREQ(KeyMnemonic.SYSREQ),
    /** Print key. */
    PRINT(KeyMnemonic.PRINT),

    /** Program Attention key 1. */
    PA1(KeyMnemonic.PA1),
    /** Program Attention key 2. */
    PA2(KeyMnemonic.PA2),
    /** Program Attention key 3. */
    PA3(KeyMnemonic.PA3),

    /** Function key F1. */
    PF1(KeyMnemonic.PF1),
    /** Function key F2. */
    PF2(KeyMnemonic.PF2),
    /** Function key F3. */
    PF3(KeyMnemonic.PF3),
    /** Function key F4. */
    PF4(KeyMnemonic.PF4),
    /** Function key F5. */
    PF5(KeyMnemonic.PF5),
    /** Function key F6. */
    PF6(KeyMnemonic.PF6),
    /** Function key F7. */
    PF7(KeyMnemonic.PF7),
    /** Function key F8. */
    PF8(KeyMnemonic.PF8),
    /** Function key F9. */
    PF9(KeyMnemonic.PF9),
    /** Function key F10. */
    PF10(KeyMnemonic.PF10),
    /** Function key F11. */
    PF11(KeyMnemonic.PF11),
    /** Function key F12. */
    PF12(KeyMnemonic.PF12),
    /** Function key F13. */
    PF13(KeyMnemonic.PF13),
    /** Function key F14. */
    PF14(KeyMnemonic.PF14),
    /** Function key F15. */
    PF15(KeyMnemonic.PF15),
    /** Function key F16. */
    PF16(KeyMnemonic.PF16),
    /** Function key F17. */
    PF17(KeyMnemonic.PF17),
    /** Function key F18. */
    PF18(KeyMnemonic.PF18),
    /** Function key F19. */
    PF19(KeyMnemonic.PF19),
    /** Function key F20. */
    PF20(KeyMnemonic.PF20),
    /** Function key F21. */
    PF21(KeyMnemonic.PF21),
    /** Function key F22. */
    PF22(KeyMnemonic.PF22),
    /** Function key F23. */
    PF23(KeyMnemonic.PF23),
    /** Function key F24. */
    PF24(KeyMnemonic.PF24);

    private final KeyMnemonic mnemonic;

    Key(KeyMnemonic mnemonic) {
        this.mnemonic = mnemonic;
    }

    /**
     * Returns the tn5250j mnemonic this key maps to.
     *
     * @return the mnemonic
     */
    public KeyMnemonic mnemonic() {
        return mnemonic;
    }

    /**
     * Returns the mnemonic token string sent to the emulator.
     *
     * @return the mnemonic token
     */
    public String token() {
        return mnemonic.mnemonic;
    }
}
