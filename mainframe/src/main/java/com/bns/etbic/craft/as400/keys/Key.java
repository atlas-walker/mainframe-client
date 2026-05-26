package com.bns.etbic.craft.as400.keys;

import org.tn5250j.keyboard.KeyMnemonic;

public enum Key {

    ENTER(KeyMnemonic.ENTER),
    TAB(KeyMnemonic.TAB),
    BACK_TAB(KeyMnemonic.BACK_TAB),
    BACKSPACE(KeyMnemonic.BACK_SPACE),
    DELETE(KeyMnemonic.DELETE),
    INSERT(KeyMnemonic.INSERT),
    HOME(KeyMnemonic.HOME),
    END_OF_FIELD(KeyMnemonic.END_OF_FIELD),
    BEGIN_OF_FIELD(KeyMnemonic.BEGIN_OF_FIELD),
    ERASE_EOF(KeyMnemonic.ERASE_EOF),
    ERASE_FIELD(KeyMnemonic.ERASE_FIELD),
    FIELD_EXIT(KeyMnemonic.FIELD_EXIT),
    FIELD_PLUS(KeyMnemonic.FIELD_PLUS),
    FIELD_MINUS(KeyMnemonic.FIELD_MINUS),
    NEW_LINE(KeyMnemonic.NEW_LINE),

    UP(KeyMnemonic.UP),
    DOWN(KeyMnemonic.DOWN),
    LEFT(KeyMnemonic.LEFT),
    RIGHT(KeyMnemonic.RIGHT),

    CLEAR(KeyMnemonic.CLEAR),
    HELP(KeyMnemonic.HELP),
    PAGE_UP(KeyMnemonic.PAGE_UP),
    PAGE_DOWN(KeyMnemonic.PAGE_DOWN),
    ROLL_LEFT(KeyMnemonic.ROLL_LEFT),
    ROLL_RIGHT(KeyMnemonic.ROLL_RIGHT),

    RESET(KeyMnemonic.RESET),
    ATTN(KeyMnemonic.ATTN),
    SYSREQ(KeyMnemonic.SYSREQ),
    PRINT(KeyMnemonic.PRINT),

    PA1(KeyMnemonic.PA1),
    PA2(KeyMnemonic.PA2),
    PA3(KeyMnemonic.PA3),

    PF1(KeyMnemonic.PF1),
    PF2(KeyMnemonic.PF2),
    PF3(KeyMnemonic.PF3),
    PF4(KeyMnemonic.PF4),
    PF5(KeyMnemonic.PF5),
    PF6(KeyMnemonic.PF6),
    PF7(KeyMnemonic.PF7),
    PF8(KeyMnemonic.PF8),
    PF9(KeyMnemonic.PF9),
    PF10(KeyMnemonic.PF10),
    PF11(KeyMnemonic.PF11),
    PF12(KeyMnemonic.PF12),
    PF13(KeyMnemonic.PF13),
    PF14(KeyMnemonic.PF14),
    PF15(KeyMnemonic.PF15),
    PF16(KeyMnemonic.PF16),
    PF17(KeyMnemonic.PF17),
    PF18(KeyMnemonic.PF18),
    PF19(KeyMnemonic.PF19),
    PF20(KeyMnemonic.PF20),
    PF21(KeyMnemonic.PF21),
    PF22(KeyMnemonic.PF22),
    PF23(KeyMnemonic.PF23),
    PF24(KeyMnemonic.PF24);

    private final KeyMnemonic mnemonic;

    Key(KeyMnemonic mnemonic) {
        this.mnemonic = mnemonic;
    }

    public KeyMnemonic mnemonic() {
        return mnemonic;
    }

    public String token() {
        return mnemonic.mnemonic;
    }
}
