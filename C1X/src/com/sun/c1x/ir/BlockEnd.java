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
package com.sun.c1x.ir;

import com.sun.c1x.util.Util;
import com.sun.c1x.util.InstructionClosure;
import com.sun.c1x.value.ValueType;
import com.sun.c1x.value.ValueStack;

import java.util.List;
import java.util.ArrayList;

/**
 * The <code>BlockEnd</code> instruction is a base class for all instructions that end a basic
 * block, including branches, switches, throws, and gotos.
 *
 * @author Ben L. Titzer
 */
public abstract class BlockEnd extends StateSplit {

    BlockBegin _begin;
    List<BlockBegin> _successors;
    final ValueStack _stateBefore;

    /**
     * Constructs a new block end with the specified value type.
     * @param type the type of the value produced by this instruction
     * @param stateBefore the value stack before the this instruction
     * @param isSafepoint <code>true</code> if this instruction is a safepoint instruction
     */
    public BlockEnd(ValueType type, ValueStack stateBefore, boolean isSafepoint) {
        super(type);
        _successors = new ArrayList<BlockBegin>(2);
        _stateBefore = stateBefore;
        if (isSafepoint) {
            setFlag(Instruction.Flag.IsSafepoint);
        }
    }

    /**
     * Get the state before the end of this block.
     * @return the value stack representing the state
     */
    public ValueStack stateBefore() {
        return _stateBefore;
    }

    /**
     * Checks whether this instruction is a safepoint.
     * @return <code>true</code> if this instruction is a safepoint
     */
    public boolean isSafepoint() {
        return checkFlag(Instruction.Flag.IsSafepoint);
    }

    /**
     * Gets the block begin associated with this block end.
     * @return the beginning of this basic block
     */
    public BlockBegin begin() {
        return _begin;
    }

    /**
     * Sets the basic block beginning for this block end. Note that this
     * method copies the successors from that basic block for the successors
     * of this basic block.
     * @param block the beginning of this basic block
     */
    public void setBegin(BlockBegin block) {
        _begin = block;
    }

    /**
     * Substitutes a successor block in this block end's successor list. Note that
     * this method updates all occurrences in the list.
     * @param oldSucc the old successor to replace
     * @param newSucc the new successor
     */
    public void substituteSuccessor(BlockBegin oldSucc, BlockBegin newSucc) {
        Util.replaceInList(oldSucc, newSucc, _successors);
    }

    /**
     * Gets the successor corresponding to the default (fall through) case.
     * @return the default successor
     */
    public BlockBegin defaultSuccessor() {
        return _successors.get(_successors.size() - 1);
    }

    /**
     * Searches for the specified successor and returns its index into the
     * successor list if found.
     * @param b the block to search for in the successor list
     * @return the index of the block in the list if found; </code>-1</code> otherwise
     */
    public int successorIndex(BlockBegin b) {
        final int max = _successors.size();
        for (int i = 0; i < max; i++) {
            if (_successors.get(i) == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets this block end's list of successors.
     * @return the successor list
     */
    public List<BlockBegin> successors() {
        return _successors;
    }

    @Override
    public void otherValuesDo(InstructionClosure closure) {
        if (_stateBefore != null) {
            _stateBefore.valuesDo(closure);
        }
    }
}