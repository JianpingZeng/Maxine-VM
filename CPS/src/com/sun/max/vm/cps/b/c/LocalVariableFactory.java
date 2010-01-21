/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
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
package com.sun.max.vm.cps.b.c;

import com.sun.max.vm.cps.b.c.JavaSlots.*;
import com.sun.max.vm.cps.cir.variable.*;
import com.sun.max.vm.type.*;

/**
 * Factory for variables that correspond to Java stack frame locals.
 *
 * @author Bernd Mathiske
 */
class LocalVariableFactory extends SlotVariableFactory {

    final CirVariable[] parameters;

    LocalVariableFactory(CirVariableFactory variableFactory, int nMaxLocals, CirVariable[] parameters) {
        super(variableFactory, nMaxLocals);
        this.parameters = parameters;
        int slotIndex = 0;
        for (int i = 0; i < parameters.length - 2; i++) {
            setVariable(slotIndex, parameters[i]);
            slotIndex++;
            if (parameters[i].isCategory2()) {
                slotIndex++;
            }
        }
    }

    void copyParametersInto(JavaStackSlot[] slots) {
        int slotIndex = 0;
        for (int i = 0; i < parameters.length - 2; i++) {
            slots[slotIndex] = new VariableJavaStackSlot(parameters[i]);
            slotIndex++;
            if (parameters[i].isCategory2()) {
                slotIndex++;
            }
        }
    }

    @Override
    protected CirVariable createSlotVariable(Kind kind, int slotIndex) {
        return cirVariableFactory.createLocalVariable(kind, slotIndex);
    }
}