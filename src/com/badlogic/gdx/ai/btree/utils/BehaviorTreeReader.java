/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.btree.utils;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class BehaviorTreeReader {
    private static final String LOG_TAG = "BehaviorTreeReader";
    protected boolean debug = false;
    protected int lineNumber;
    protected boolean reportsComments;
    private static final byte[] _btree_actions = BehaviorTreeReader.init__btree_actions_0();
    private static final short[] _btree_key_offsets = BehaviorTreeReader.init__btree_key_offsets_0();
    private static final char[] _btree_trans_keys = BehaviorTreeReader.init__btree_trans_keys_0();
    private static final byte[] _btree_single_lengths = BehaviorTreeReader.init__btree_single_lengths_0();
    private static final byte[] _btree_range_lengths = BehaviorTreeReader.init__btree_range_lengths_0();
    private static final short[] _btree_index_offsets = BehaviorTreeReader.init__btree_index_offsets_0();
    private static final byte[] _btree_indicies = BehaviorTreeReader.init__btree_indicies_0();
    private static final byte[] _btree_trans_targs = BehaviorTreeReader.init__btree_trans_targs_0();
    private static final byte[] _btree_trans_actions = BehaviorTreeReader.init__btree_trans_actions_0();
    private static final byte[] _btree_eof_actions = BehaviorTreeReader.init__btree_eof_actions_0();
    static final int btree_start = 29;
    static final int btree_first_final = 29;
    static final int btree_error = 0;
    static final int btree_en_main = 29;

    protected abstract void startLine(int var1);

    protected abstract void startStatement(String var1, boolean var2, boolean var3);

    protected abstract void attribute(String var1, Object var2);

    protected abstract void endStatement();

    protected abstract void endLine();

    protected void comment(String text) {
    }

    public BehaviorTreeReader() {
        this(false);
    }

    public BehaviorTreeReader(boolean reportsComments) {
        this.reportsComments = reportsComments;
    }

    public void parse(String string) {
        char[] data = string.toCharArray();
        this.parse(data, 0, data.length);
    }

    public void parse(Reader reader) {
        try {
            int length;
            char[] data = new char[1024];
            int offset = 0;
            while ((length = reader.read(data, offset, data.length - offset)) != -1) {
                if (length == 0) {
                    char[] newData = new char[data.length * 2];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    data = newData;
                    continue;
                }
                offset += length;
            }
            this.parse(data, 0, offset);
        }
        catch (IOException ex) {
            throw new SerializationException(ex);
        }
        finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    public void parse(InputStream input) {
        try {
            this.parse(new InputStreamReader(input, "UTF-8"));
        }
        catch (IOException ex) {
            throw new SerializationException(ex);
        }
        finally {
            StreamUtils.closeQuietly(input);
        }
    }

    public void parse(FileHandle file) {
        try {
            this.parse(file.reader("UTF-8"));
        }
        catch (Exception ex) {
            throw new SerializationException("Error parsing file: " + file, ex);
        }
    }

    /*
     * Unable to fully structure code
     */
    public void parse(char[] data, int offset, int length) {
        p = offset;
        eof = pe = length;
        s = 0;
        indent = 0;
        taskIndex = -1;
        isGuard = false;
        isSubtreeRef = false;
        statementName = null;
        taskProcessed = false;
        needsUnescape = false;
        stringIsUnquoted = false;
        parseRuntimeEx = null;
        attrName = null;
        this.lineNumber = 1;
        try {
            cs = 29;
            _trans = 0;
            _goto_targ = 0;
            block45: while (true) {
                switch (_goto_targ) {
                    case 0: {
                        if (p == pe) {
                            _goto_targ = 4;
                            continue block45;
                        }
                        if (cs == 0) {
                            _goto_targ = 5;
                            continue block45;
                        }
                    }
                    case 1: {
                        _keys = BehaviorTreeReader._btree_key_offsets[cs];
                        _trans = BehaviorTreeReader._btree_index_offsets[cs];
                        _klen = BehaviorTreeReader._btree_single_lengths[cs];
                        if (_klen <= 0) ** GOTO lbl47
                        _lower = _keys;
                        _upper = _keys + _klen - 1;
                        while (_upper >= _lower) {
                            _mid = _lower + (_upper - _lower >> 1);
                            if (data[p] < BehaviorTreeReader._btree_trans_keys[_mid]) {
                                _upper = _mid - 1;
                                continue;
                            }
                            if (data[p] > BehaviorTreeReader._btree_trans_keys[_mid]) {
                                _lower = _mid + 1;
                                continue;
                            }
                            _trans += _mid - _keys;
                            ** GOTO lbl61
                        }
                        _keys += _klen;
                        _trans += _klen;
lbl47:
                        // 2 sources

                        if ((_klen = BehaviorTreeReader._btree_range_lengths[cs]) > 0) {
                            _lower = _keys;
                            _upper = _keys + (_klen << 1) - 2;
                            while (_upper >= _lower) {
                                _mid = _lower + (_upper - _lower >> 1 & -2);
                                if (data[p] < BehaviorTreeReader._btree_trans_keys[_mid]) {
                                    _upper = _mid - 2;
                                    continue;
                                }
                                if (data[p] > BehaviorTreeReader._btree_trans_keys[_mid + 1]) {
                                    _lower = _mid + 2;
                                    continue;
                                }
                                _trans += _mid - _keys >> 1;
                                ** GOTO lbl61
                            }
                            _trans += _klen;
                        }
lbl61:
                        // 5 sources

                        _trans = BehaviorTreeReader._btree_indicies[_trans];
                        cs = BehaviorTreeReader._btree_trans_targs[_trans];
                        if (BehaviorTreeReader._btree_trans_actions[_trans] == 0) ** GOTO lbl199
                        _acts = BehaviorTreeReader._btree_trans_actions[_trans];
                        _nacts = BehaviorTreeReader._btree_actions[_acts++];
                        while (_nacts-- > 0) {
                            switch (BehaviorTreeReader._btree_actions[_acts++]) {
                                case 0: {
                                    value = new String(data, s, p - s);
                                    s = p;
                                    if (needsUnescape) {
                                        value = BehaviorTreeReader.unescape(value);
                                    }
                                    if (stringIsUnquoted) {
                                        if (this.debug) {
                                            GdxAI.getLogger().info("BehaviorTreeReader", "string: " + attrName + "=" + value);
                                        }
                                        if (value.equals("true")) {
                                            if (this.debug) {
                                                GdxAI.getLogger().info("BehaviorTreeReader", "boolean: " + attrName + "=true");
                                            }
                                            this.attribute(attrName, Boolean.TRUE);
                                        } else if (value.equals("false")) {
                                            if (this.debug) {
                                                GdxAI.getLogger().info("BehaviorTreeReader", "boolean: " + attrName + "=false");
                                            }
                                            this.attribute(attrName, Boolean.FALSE);
                                        } else if (value.equals("null")) {
                                            this.attribute(attrName, null);
                                        } else {
                                            try {
                                                if (BehaviorTreeReader.containsFloatingPointCharacters(value)) {
                                                    if (this.debug) {
                                                        GdxAI.getLogger().info("BehaviorTreeReader", "double: " + attrName + "=" + Double.parseDouble(value));
                                                    }
                                                    this.attribute(attrName, new Double(value));
                                                }
                                                if (this.debug) {
                                                    GdxAI.getLogger().info("BehaviorTreeReader", "double: " + attrName + "=" + Double.parseDouble(value));
                                                }
                                                this.attribute(attrName, new Long(value));
                                            }
                                            catch (NumberFormatException nfe) {
                                                throw new GdxRuntimeException("Attribute value must be a number, a boolean, a string or null");
                                            }
                                        }
                                    } else {
                                        if (this.debug) {
                                            GdxAI.getLogger().info("BehaviorTreeReader", "string: " + attrName + "=\"" + value + "\"");
                                        }
                                        this.attribute(attrName, value);
                                    }
                                    stringIsUnquoted = false;
                                    break;
                                }
                                case 1: {
                                    if (this.debug) {
                                        GdxAI.getLogger().info("BehaviorTreeReader", "unquotedChars");
                                    }
                                    s = p;
                                    needsUnescape = false;
                                    stringIsUnquoted = true;
                                    block49: while (true) {
                                        switch (data[p]) {
                                            case '\\': {
                                                needsUnescape = true;
                                                ** GOTO lbl119
                                            }
                                            case '\t': 
                                            case '\n': 
                                            case '\r': 
                                            case ' ': 
                                            case '(': 
                                            case ')': {
                                                break block49;
                                            }
lbl119:
                                            // 2 sources

                                            default: {
                                                if (++p != eof) continue block49;
                                                break block49;
                                            }
                                        }
                                        break;
                                    }
                                    --p;
                                    break;
                                }
                                case 2: {
                                    if (this.debug) {
                                        GdxAI.getLogger().info("BehaviorTreeReader", "quotedChars");
                                    }
                                    s = ++p;
                                    needsUnescape = false;
                                    block50: while (true) {
                                        switch (data[p]) {
                                            case '\\': {
                                                needsUnescape = true;
                                                ++p;
                                                ** GOTO lbl137
                                            }
                                            case '\"': {
                                                break block50;
                                            }
lbl137:
                                            // 2 sources

                                            default: {
                                                if (++p != eof) continue block50;
                                                break block50;
                                            }
                                        }
                                        break;
                                    }
                                    --p;
                                    break;
                                }
                                case 3: {
                                    indent = 0;
                                    taskIndex = -1;
                                    isGuard = false;
                                    isSubtreeRef = false;
                                    statementName = null;
                                    taskProcessed = false;
                                    ++this.lineNumber;
                                    if (!this.debug) break;
                                    GdxAI.getLogger().info("BehaviorTreeReader", "****NEWLINE**** " + this.lineNumber);
                                    break;
                                }
                                case 4: {
                                    ++indent;
                                    break;
                                }
                                case 5: {
                                    if (taskIndex >= 0) {
                                        this.endStatement();
                                    }
                                    taskProcessed = true;
                                    if (statementName != null) {
                                        this.endLine();
                                    }
                                    if (!this.debug) break;
                                    GdxAI.getLogger().info("BehaviorTreeReader", "endLine: indent: " + indent + " taskName: " + statementName + " data[" + p + "] = " + (p >= eof ? "EOF" : "\"" + data[p] + "\""));
                                    break;
                                }
                                case 6: {
                                    s = p;
                                    break;
                                }
                                case 7: {
                                    if (this.reportsComments) {
                                        this.comment(new String(data, s, p - s));
                                        break;
                                    }
                                    if (!this.debug) break;
                                    GdxAI.getLogger().info("BehaviorTreeReader", "# Comment");
                                    break;
                                }
                                case 8: {
                                    if (taskIndex++ < 0) {
                                        this.startLine(indent);
                                    } else {
                                        this.endStatement();
                                    }
                                    statementName = new String(data, s, p - s);
                                    this.startStatement(statementName, isSubtreeRef, isGuard);
                                    isGuard = false;
                                    break;
                                }
                                case 9: {
                                    attrName = new String(data, s, p - s);
                                    break;
                                }
                                case 10: {
                                    isSubtreeRef = false;
                                    break;
                                }
                                case 11: {
                                    isSubtreeRef = true;
                                    break;
                                }
                                case 12: {
                                    isGuard = true;
                                    break;
                                }
                                case 13: {
                                    isGuard = false;
                                }
                            }
                        }
                    }
lbl199:
                    // 3 sources

                    case 2: {
                        if (cs == 0) {
                            _goto_targ = 5;
                            continue block45;
                        }
                        if (++p != pe) {
                            _goto_targ = 1;
                            continue block45;
                        }
                    }
                    case 4: {
                        if (p != eof) break block45;
                        __acts = BehaviorTreeReader._btree_eof_actions[cs];
                        __nacts = BehaviorTreeReader._btree_actions[__acts++];
                        while (__nacts-- > 0) {
                            switch (BehaviorTreeReader._btree_actions[__acts++]) {
                                case 0: {
                                    value = new String(data, s, p - s);
                                    s = p;
                                    if (needsUnescape) {
                                        value = BehaviorTreeReader.unescape(value);
                                    }
                                    if (stringIsUnquoted) {
                                        if (this.debug) {
                                            GdxAI.getLogger().info("BehaviorTreeReader", "string: " + attrName + "=" + value);
                                        }
                                        if (value.equals("true")) {
                                            if (this.debug) {
                                                GdxAI.getLogger().info("BehaviorTreeReader", "boolean: " + attrName + "=true");
                                            }
                                            this.attribute(attrName, Boolean.TRUE);
                                        } else if (value.equals("false")) {
                                            if (this.debug) {
                                                GdxAI.getLogger().info("BehaviorTreeReader", "boolean: " + attrName + "=false");
                                            }
                                            this.attribute(attrName, Boolean.FALSE);
                                        } else if (value.equals("null")) {
                                            this.attribute(attrName, null);
                                        } else {
                                            try {
                                                if (BehaviorTreeReader.containsFloatingPointCharacters(value)) {
                                                    if (this.debug) {
                                                        GdxAI.getLogger().info("BehaviorTreeReader", "double: " + attrName + "=" + Double.parseDouble(value));
                                                    }
                                                    this.attribute(attrName, new Double(value));
                                                }
                                                if (this.debug) {
                                                    GdxAI.getLogger().info("BehaviorTreeReader", "double: " + attrName + "=" + Double.parseDouble(value));
                                                }
                                                this.attribute(attrName, new Long(value));
                                            }
                                            catch (NumberFormatException nfe) {
                                                throw new GdxRuntimeException("Attribute value must be a number, a boolean, a string or null");
                                            }
                                        }
                                    } else {
                                        if (this.debug) {
                                            GdxAI.getLogger().info("BehaviorTreeReader", "string: " + attrName + "=\"" + value + "\"");
                                        }
                                        this.attribute(attrName, value);
                                    }
                                    stringIsUnquoted = false;
                                    break;
                                }
                                case 5: {
                                    if (taskIndex >= 0) {
                                        this.endStatement();
                                    }
                                    taskProcessed = true;
                                    if (statementName != null) {
                                        this.endLine();
                                    }
                                    if (!this.debug) break;
                                    GdxAI.getLogger().info("BehaviorTreeReader", "endLine: indent: " + indent + " taskName: " + statementName + " data[" + p + "] = " + (p >= eof ? "EOF" : "\"" + data[p] + "\""));
                                    break;
                                }
                                case 6: {
                                    s = p;
                                    break;
                                }
                                case 7: {
                                    if (this.reportsComments) {
                                        this.comment(new String(data, s, p - s));
                                        break;
                                    }
                                    if (!this.debug) break;
                                    GdxAI.getLogger().info("BehaviorTreeReader", "# Comment");
                                    break;
                                }
                                case 8: {
                                    if (taskIndex++ < 0) {
                                        this.startLine(indent);
                                    } else {
                                        this.endStatement();
                                    }
                                    statementName = new String(data, s, p - s);
                                    this.startStatement(statementName, isSubtreeRef, isGuard);
                                    isGuard = false;
                                    break;
                                }
                                case 10: {
                                    isSubtreeRef = false;
                                    break;
                                }
                                case 11: {
                                    isSubtreeRef = true;
                                }
                            }
                        }
                        break block45;
                    }
                }
                break;
            }
        }
        catch (RuntimeException ex) {
            parseRuntimeEx = ex;
        }
        if (p < pe || statementName != null && !taskProcessed) {
            throw new SerializationException("Error parsing behavior tree on line " + this.lineNumber + " near: " + new String(data, p, pe - p), parseRuntimeEx);
        }
        if (parseRuntimeEx != null) {
            throw new SerializationException("Error parsing behavior tree: " + new String(data), parseRuntimeEx);
        }
    }

    private static byte[] init__btree_actions_0() {
        return new byte[]{0, 1, 0, 1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 9, 1, 12, 1, 13, 2, 0, 5, 2, 0, 13, 2, 5, 3, 2, 7, 5, 2, 10, 8, 2, 11, 8, 3, 0, 5, 3, 3, 6, 7, 5, 3, 7, 5, 3, 3, 10, 8, 5, 3, 10, 8, 13, 3, 11, 8, 5, 3, 11, 8, 13, 4, 6, 7, 5, 3, 4, 10, 8, 5, 3, 4, 11, 8, 5, 3};
    }

    private static short[] init__btree_key_offsets_0() {
        return new short[]{0, 0, 1, 6, 16, 21, 33, 37, 47, 59, 63, 72, 73, 77, 82, 87, 91, 105, 114, 126, 130, 139, 143, 144, 148, 152, 157, 170, 174, 179, 191, 196, 198, 200, 213, 218, 233, 243, 248, 253, 267};
    }

    private static char[] init__btree_trans_keys_0() {
        return new char[]{'\n', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', '$', ')', '_', 'A', 'Z', 'a', 'z', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ')', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ')', '\t', '\r', ' ', '$', '(', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ':', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ':', '\t', '\n', '\r', ' ', '\"', '#', ':', '(', ')', '\"', '\t', '\r', ' ', ':', '_', 'A', 'Z', 'a', 'z', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ')', '\t', '\r', ' ', '$', ')', '.', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ')', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ':', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ':', '\t', '\n', '\r', ' ', '\"', '#', ':', '(', ')', '\t', '\r', ' ', ')', '\"', '\t', '\r', ' ', ')', '\t', '\r', ' ', ':', '_', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', '$', ')', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\r', ' ', ')', '_', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '$', '(', '_', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '\n', '\r', '\n', '\r', '\t', '\n', '\r', ' ', '#', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '\t', '\n', '\r', ' ', '#', '$', '.', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '_', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '\t', '\n', '\r', ' ', '#', '\t', '\n', '\r', ' ', '#', '$', '?', '_', '0', '9', 'A', 'Z', 'a', 'z', '\t', '\n', '\r', ' ', '#', '\u0000'};
    }

    private static byte[] init__btree_single_lengths_0() {
        return new byte[]{0, 1, 1, 6, 1, 6, 4, 6, 6, 4, 7, 1, 4, 1, 1, 4, 8, 5, 6, 4, 7, 4, 1, 4, 4, 1, 7, 4, 1, 8, 5, 2, 2, 7, 5, 9, 6, 5, 5, 8, 5};
    }

    private static byte[] init__btree_range_lengths_0() {
        return new byte[]{0, 0, 2, 2, 2, 3, 0, 2, 3, 0, 1, 0, 0, 2, 2, 0, 3, 2, 3, 0, 1, 0, 0, 0, 0, 2, 3, 0, 2, 2, 0, 0, 0, 3, 0, 3, 2, 0, 0, 3, 0};
    }

    private static short[] init__btree_index_offsets_0() {
        return new short[]{0, 0, 2, 6, 15, 19, 29, 34, 43, 53, 58, 67, 69, 74, 78, 82, 87, 99, 107, 117, 122, 131, 136, 138, 143, 148, 152, 163, 168, 172, 183, 189, 192, 195, 206, 212, 225, 234, 240, 246, 258};
    }

    private static byte[] init__btree_indicies_0() {
        return new byte[]{0, 1, 2, 2, 2, 1, 3, 3, 3, 4, 5, 6, 6, 6, 1, 7, 7, 7, 1, 8, 8, 8, 9, 11, 10, 10, 10, 10, 1, 12, 12, 12, 5, 1, 13, 13, 13, 14, 15, 16, 16, 16, 1, 17, 17, 17, 19, 20, 18, 18, 18, 18, 1, 21, 21, 21, 22, 1, 22, 1, 22, 22, 24, 1, 1, 1, 23, 25, 1, 17, 17, 17, 19, 1, 26, 26, 26, 1, 27, 27, 27, 1, 8, 8, 8, 9, 1, 28, 28, 28, 29, 30, 31, 33, 32, 32, 32, 32, 1, 34, 34, 34, 5, 35, 35, 35, 1, 36, 36, 36, 38, 39, 37, 37, 37, 37, 1, 40, 40, 40, 41, 1, 41, 1, 41, 41, 43, 1, 1, 1, 42, 44, 44, 44, 45, 1, 46, 1, 34, 34, 34, 5, 1, 36, 36, 36, 38, 1, 47, 47, 47, 1, 28, 28, 28, 29, 30, 33, 47, 47, 47, 47, 1, 28, 28, 28, 30, 1, 32, 32, 32, 1, 48, 49, 50, 48, 51, 14, 15, 16, 16, 16, 1, 50, 49, 50, 50, 51, 1, 53, 54, 52, 56, 57, 55, 58, 59, 58, 58, 60, 62, 61, 61, 61, 61, 1, 58, 59, 58, 58, 60, 1, 63, 64, 63, 63, 65, 66, 67, 68, 27, 27, 27, 27, 1, 69, 49, 69, 69, 51, 70, 70, 70, 1, 71, 72, 71, 71, 73, 1, 69, 49, 69, 69, 51, 1, 63, 64, 63, 63, 65, 66, 68, 26, 26, 26, 26, 1, 63, 64, 63, 63, 65, 1, 0};
    }

    private static byte[] init__btree_trans_targs_0() {
        return new byte[]{29, 0, 33, 3, 4, 7, 16, 5, 6, 7, 5, 15, 6, 7, 2, 3, 35, 9, 8, 10, 12, 9, 10, 37, 11, 38, 39, 35, 17, 25, 7, 28, 16, 27, 17, 18, 19, 18, 20, 24, 19, 20, 21, 22, 17, 7, 23, 26, 29, 29, 30, 31, 32, 29, 1, 32, 29, 1, 30, 29, 31, 33, 34, 36, 29, 31, 13, 14, 40, 36, 8, 36, 29, 31};
    }

    private static byte[] init__btree_trans_actions_0() {
        return new byte[]{7, 0, 13, 0, 0, 19, 13, 13, 36, 63, 0, 0, 0, 0, 0, 17, 13, 15, 0, 15, 0, 0, 0, 3, 5, 1, 0, 0, 33, 0, 55, 0, 0, 0, 0, 13, 15, 0, 15, 0, 0, 0, 3, 5, 1, 24, 1, 0, 9, 27, 0, 0, 13, 67, 43, 0, 47, 30, 36, 77, 36, 0, 0, 33, 72, 33, 0, 0, 0, 0, 13, 1, 39, 1};
    }

    private static byte[] init__btree_eof_actions_0() {
        return new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 11, 43, 30, 59, 59, 51, 11, 21, 11, 51, 51};
    }

    private static boolean containsFloatingPointCharacters(String value) {
        int n = value.length();
        for (int i = 0; i < n; ++i) {
            switch (value.charAt(i)) {
                case '.': 
                case 'E': 
                case 'e': {
                    return true;
                }
            }
        }
        return false;
    }

    private static String unescape(String value) {
        int length = value.length();
        StringBuilder buffer = new StringBuilder(length + 16);
        int i = 0;
        while (i < length) {
            char c;
            if ((c = value.charAt(i++)) != '\\') {
                buffer.append(c);
                continue;
            }
            if (i == length) break;
            if ((c = value.charAt(i++)) == 'u') {
                buffer.append(Character.toChars(Integer.parseInt(value.substring(i, i + 4), 16)));
                i += 4;
                continue;
            }
            switch (c) {
                case '\"': 
                case '/': 
                case '\\': {
                    break;
                }
                case 'b': {
                    c = '\b';
                    break;
                }
                case 'f': {
                    c = '\f';
                    break;
                }
                case 'n': {
                    c = '\n';
                    break;
                }
                case 'r': {
                    c = '\r';
                    break;
                }
                case 't': {
                    c = '\t';
                    break;
                }
                default: {
                    throw new SerializationException("Illegal escaped character: \\" + c);
                }
            }
            buffer.append(c);
        }
        return buffer.toString();
    }
}

