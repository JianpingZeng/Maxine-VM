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
package com.sun.max.vm;

import static com.sun.max.vm.MaxineVM.*;
import static com.sun.max.vm.compiler.CallEntryPoint.*;

import java.io.*;
import java.util.*;

import com.sun.max.*;
import com.sun.max.annotate.*;
import com.sun.max.asm.*;
import com.sun.max.platform.*;
import com.sun.max.program.*;
import com.sun.max.vm.compiler.*;
import com.sun.max.vm.compiler.target.*;
import com.sun.max.vm.heap.*;
import com.sun.max.vm.layout.*;
import com.sun.max.vm.monitor.*;
import com.sun.max.vm.reference.*;
import com.sun.max.vm.run.*;
import com.sun.max.vm.runtime.*;
import com.sun.max.vm.trampoline.*;

/**
 * The configuration of a VM, which includes the schemes, safepoint mechanism, etc.
 *
 * @author Bernd Mathiske
 * @author Ben L. Titzer
 */
public final class VMConfiguration {

    public final BuildLevel buildLevel;
    public final Platform platform;
    public final VMPackage referencePackage;
    public final VMPackage layoutPackage;
    public final VMPackage heapPackage;
    public final VMPackage monitorPackage;
    public final VMPackage bootCompilerPackage;
    public final VMPackage jitCompilerPackage;
    public final VMPackage optCompilerPackage;
    public final VMPackage compilationPackage;
    public final VMPackage trampolinePackage;
    public final VMPackage targetABIsPackage;
    public final VMPackage runPackage;
    public final Safepoint safepoint;
    public final TrapStateAccess trapStateAccess;

    private ArrayList<VMScheme> vmSchemes = new ArrayList<VMScheme>();
    private boolean areSchemesLoadedAndInstantiated = false;

    @CONSTANT_WHEN_NOT_ZERO
    private ReferenceScheme referenceScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private LayoutScheme layoutScheme;
    @CONSTANT_WHEN_NOT_ZERO
    private HeapScheme heapScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private MonitorScheme monitorScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private BootstrapCompilerScheme bootCompilerScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private RuntimeCompilerScheme jitCompilerScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private RuntimeCompilerScheme optCompilerScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private CompilationScheme compilationScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private DynamicTrampolineScheme trampolineScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private TargetABIsScheme targetABIsScheme = null;
    @CONSTANT_WHEN_NOT_ZERO
    private RunScheme runScheme = null;

    public VMConfiguration(BuildLevel buildLevel,
                           Platform platform,
                           VMPackage referencePackage,
                           VMPackage layoutPackage,
                           VMPackage heapPackage,
                           VMPackage monitorPackage,
                           VMPackage bootCompilerPackage,
                           VMPackage jitCompilerPackage,
                           VMPackage optCompilerPackage,
                           VMPackage compilationPackage,
                           VMPackage trampolinePackage,
                           VMPackage targetABIsPackage, VMPackage runPackage) {
        this.buildLevel = buildLevel;
        this.platform = platform;
        this.referencePackage = referencePackage;
        this.layoutPackage = layoutPackage;
        this.heapPackage = heapPackage;
        this.monitorPackage = monitorPackage;
        this.bootCompilerPackage = bootCompilerPackage;
        this.jitCompilerPackage = jitCompilerPackage;
        this.optCompilerPackage = optCompilerPackage;
        this.compilationPackage = compilationPackage;
        this.trampolinePackage = trampolinePackage;
        this.targetABIsPackage = targetABIsPackage;
        this.runPackage = runPackage;
        this.safepoint = Safepoint.create(this);
        this.trapStateAccess = TrapStateAccess.create(this);
    }

    @INLINE
    public ReferenceScheme referenceScheme() {
        return referenceScheme;
    }

    @INLINE
    public LayoutScheme layoutScheme() {
        return layoutScheme;
    }

    @INLINE
    public HeapScheme heapScheme() {
        return heapScheme;
    }

    @INLINE
    public MonitorScheme monitorScheme() {
        return monitorScheme;
    }

    @INLINE
    public BootstrapCompilerScheme bootCompilerScheme() {
        return bootCompilerScheme;
    }

    @INLINE
    public RuntimeCompilerScheme jitCompilerScheme() {
        return jitCompilerScheme;
    }

    @INLINE
    public RuntimeCompilerScheme optCompilerScheme() {
        return optCompilerScheme;
    }

    @INLINE
    public CompilationScheme compilationScheme() {
        return compilationScheme;
    }

    @INLINE
    public DynamicTrampolineScheme trampolineScheme() {
        return trampolineScheme;
    }

    @INLINE
    public TargetABIsScheme  targetABIsScheme() {
        return targetABIsScheme;
    }

    @INLINE
    public RunScheme runScheme() {
        return runScheme;
    }

    public List<MaxPackage> packages() {
        return Arrays.asList(new MaxPackage[] {
            referencePackage,
            layoutPackage,
            heapPackage,
            monitorPackage,
            bootCompilerPackage,
            compilationPackage,
            trampolinePackage,
            targetABIsPackage,
            runPackage});
    }

    public List<VMScheme> vmSchemes() {
        return vmSchemes;
    }

    private <VMScheme_Type extends VMScheme> VMScheme_Type loadAndInstantiateScheme(List<VMScheme> loadedSchemes, MaxPackage p, Class<VMScheme_Type> vmSchemeType, Object... arguments) {
        if (p == null) {
            throw ProgramError.unexpected("Package not found for scheme: " + vmSchemeType.getSimpleName());
        }

        if (loadedSchemes != null) {
            Class< ? extends VMScheme_Type> impl = p.loadSchemeImplementation(vmSchemeType);
            for (VMScheme vmScheme : loadedSchemes) {
                if (vmScheme.getClass() == impl) {
                    vmSchemes.add(vmScheme);
                    return vmSchemeType.cast(vmScheme);
                }
            }
        }

        final VMScheme_Type vmScheme = p.loadAndInstantiateScheme(vmSchemeType, arguments);
        vmSchemes.add(vmScheme);
        return vmScheme;
    }

    /**
     * Loads and instantiates all the schemes of this configuration.
     *
     * @param loadedSchemes the set of schemes already loaded and instantiated in this process. If non-{@code null},
     *            this list is used to prevent any given scheme implementation from being instantiated more than once.
     */
    @HOSTED_ONLY
    public void loadAndInstantiateSchemes(List<VMScheme> loadedSchemes) {
        if (areSchemesLoadedAndInstantiated) {
            return;
        }

        referenceScheme = loadAndInstantiateScheme(loadedSchemes, referencePackage, ReferenceScheme.class, this);
        layoutScheme = loadAndInstantiateScheme(loadedSchemes, layoutPackage, LayoutScheme.class, this);
        monitorScheme = loadAndInstantiateScheme(loadedSchemes, monitorPackage, MonitorScheme.class, this);
        heapScheme = loadAndInstantiateScheme(loadedSchemes, heapPackage, HeapScheme.class, this);
        targetABIsScheme = loadAndInstantiateScheme(loadedSchemes, targetABIsPackage, TargetABIsScheme.class, this);
        bootCompilerScheme = loadAndInstantiateScheme(loadedSchemes, bootCompilerPackage, BootstrapCompilerScheme.class, this);
        trampolineScheme = loadAndInstantiateScheme(loadedSchemes, trampolinePackage, DynamicTrampolineScheme.class, this);

        if (jitCompilerPackage != null) {
            jitCompilerScheme = loadAndInstantiateScheme(loadedSchemes, jitCompilerPackage, RuntimeCompilerScheme.class, this);
        } else {
            // no JIT, always using the optimizing compiler
            jitCompilerScheme = bootCompilerScheme;
        }
        if (MaxPackage.equal(optCompilerPackage, jitCompilerPackage)) {
            optCompilerScheme = jitCompilerScheme;
        } else if (MaxPackage.equal(optCompilerPackage, bootCompilerPackage)) {
            optCompilerScheme = bootCompilerScheme;
        } else if (optCompilerPackage == null) {
            optCompilerScheme = bootCompilerScheme;
        } else {
            optCompilerScheme = loadAndInstantiateScheme(loadedSchemes, optCompilerPackage, RuntimeCompilerScheme.class, this);
        }

        if (loadedSchemes == null) {
            // FIXME: This is a hack to avoid adding an "AdapterFrameScheme".
            if (needsAdapters()) {
                OPTIMIZED_ENTRY_POINT.init(8, 8);
                JIT_ENTRY_POINT.init(0, 0);
                VTABLE_ENTRY_POINT.init(OPTIMIZED_ENTRY_POINT);
                // Calls made from a C_ENTRY_POINT method link to the OPTIMIZED_ENTRY_POINT of the callee
                C_ENTRY_POINT.init(0, OPTIMIZED_ENTRY_POINT.offset());
            } else {
                CallEntryPoint.initAllToZero();
            }
        }

        compilationScheme = loadAndInstantiateScheme(loadedSchemes, compilationPackage, CompilationScheme.class, this);
        runScheme = loadAndInstantiateScheme(loadedSchemes, runPackage, RunScheme.class, this);
        areSchemesLoadedAndInstantiated = true;
    }

    /**
     * Determines if any pair of compilers in this configuration use different
     * calling conventions and thus mandate the use of {@linkplain Adapter adapters}
     * to adapt the arguments when a call crosses a calling convention boundary.
     */
    public boolean needsAdapters() {
        return optCompilerScheme.calleeEntryPoint() != bootCompilerScheme.calleeEntryPoint() ||
               optCompilerScheme.calleeEntryPoint() != jitCompilerScheme.calleeEntryPoint() ||
               bootCompilerScheme.calleeEntryPoint() != jitCompilerScheme.calleeEntryPoint();
    }

    public void initializeSchemes(MaxineVM.Phase phase) {
        for (int i = 0; i < vmSchemes.size(); i++) {
            vmSchemes.get(i).initialize(phase);
        }
    }

    public void finalizeSchemes(MaxineVM.Phase phase) {
        for (int i = 0; i < vmSchemes.size(); i++) {
            vmSchemes.get(i).finalize(phase);
        }
    }

    /**
     * Convenience method for accessing the configuration associated with the
     * current {@linkplain MaxineVM#vm() VM} context.
     * @return
     */
    @FOLD
    public static VMConfiguration vmConfig() {
        return vm().config;
    }

    @Override
    public String toString() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        print(new PrintStream(baos), "");
        return baos.toString();
    }

    public void print(PrintStream out, String indent) {
        out.println(indent + "Build level: " + buildLevel);
        out.println(indent + "Platform: " + platform);
        for (VMScheme vmScheme : vmSchemes()) {
            final String specification = vmScheme.specification().getSimpleName();
            out.println(indent + specification.replace("Scheme", " scheme") + ": " + vmScheme.getClass().getName());
        }
    }

    /**
     * Use {@link MaxineVM#isDebug()} instead of calling this directly.
     */
    @FOLD
    public boolean debugging() {
        return buildLevel == BuildLevel.DEBUG;
    }

    /**
     * Determines if a given package is considered part of the VM under this VM configuration.
     * @return {@code true} if the specified package is part of this VM in this configuration
     */
    public boolean isMaxineVMPackage(MaxPackage maxPackage) {
        if (maxPackage instanceof BasePackage) {
            return true;
        }
        if (maxPackage instanceof AsmPackage) {
            final AsmPackage asmPackage = (AsmPackage) maxPackage;
            return asmPackage.isPartOfAssembler(platform.instructionSet());
        }
        if (maxPackage instanceof VMPackage) {
            final VMPackage vmPackage = (VMPackage) maxPackage;
            return vmPackage.isPartOfMaxineVM(this);
        }
        return false;
    }
}
