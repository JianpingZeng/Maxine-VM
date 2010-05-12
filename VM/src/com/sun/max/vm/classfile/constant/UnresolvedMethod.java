/*
 * Copyright (c) 2010 Sun Microsystems, Inc.  All rights reserved.
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
package com.sun.max.vm.classfile.constant;

import com.sun.cri.ci.*;
import com.sun.cri.ri.*;

/**
 * An {@linkplain RiField#isResolved() unresolved} method with a back reference
 * to the constant pool entry from which it was derived.
 *
 * @author Doug Simon
 */
public class UnresolvedMethod extends CiUnresolvedMethod {

    public final ConstantPool constantPool;
    public final int cpi;

    public UnresolvedMethod(ConstantPool constantPool, int cpi, RiType holder, String name, RiSignature signature) {
        super(holder, name, signature);
        this.constantPool = constantPool;
        this.cpi = cpi;
    }
}