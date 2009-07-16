/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.c1x.lir;

import java.util.*;

import com.sun.c1x.ci.*;
import com.sun.c1x.util.*;

/**
 * The <code>LIRJavaCall</code> class definition.
 *
 * @author Marcelo Cintra
 *
 */
public class LIRJavaCall extends LIRCall {

    private CiMethod method;
    private LIROperand receiver;

    /**
     * Creates a new LIRJavaCall instruction.
     *
     * @param opcode
     * @param method
     * @param receiver
     * @param result
     * @param address
     * @param arguments
     * @param info
     */
    public LIRJavaCall(LIROpcode opcode, CiMethod method, LIROperand receiver, LIROperand result, long address, List<LIROperand> arguments, CodeEmitInfo info) {
        super(opcode, address, result, arguments, info);
        this.method = method;
        this.receiver = receiver;
    }

    /**
     * Returns the receiver for this method call.
     *
     * @return the receiver
     */
    public LIROperand receiver() {
        return receiver;
    }

    /**
     * Gets the method of this java call.
     *
     * @return the method
     */
    public CiMethod method() {
        return method;
    }

    /**
     * Gets the virtual table offset for his java call.
     *
     * @return the virtual table offset for this call.
     */
    public long vtableOffset() {
        assert code == LIROpcode.VirtualCall : "Only have vtable for real virtual call";
        return address();
    }

    /**
     * Emits target assembly code for this instruction.
     *
     * @param masm the target assembler
     */
    @Override
    public void emitCode(LIRAssembler masm) {
        masm.emitCall(this);
    }

    /**
     * Prints this instruction.
     *
     * @param out the output log stream
     */
    @Override
    public void printInstruction(LogStream out) {
        out.print("call: ");
        out.printf("[addr: 0x%x]", address());
        if (receiver.isValid()) {
            out.print(" [recv: ");
            receiver.print(out);
            out.print("]");
        }
        if (result.isValid()) {
            out.print(" [result: ");
            result.print(out);
            out.print("]");
        }
    }
}