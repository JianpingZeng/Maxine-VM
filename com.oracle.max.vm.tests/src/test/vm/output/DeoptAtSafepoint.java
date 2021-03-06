/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.vm.output;

import java.util.*;

import com.sun.max.lang.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.compiler.RuntimeCompiler.Nature;
import com.sun.max.vm.compiler.deopt.*;
import com.sun.max.vm.compiler.target.*;

/**
 * The primary purpose of this test is ensure Maxine doesn't crash when deoptimizing at a safepoint.
 */
public class DeoptAtSafepoint {

    public static void main(String[] args) throws InterruptedException {
        boolean isMaxine = System.getProperty("java.vm.name").startsWith("Maxine");
        Spinner s = new Spinner();
        TargetMethod tm = null;
        if (isMaxine) {
            ClassMethodActor cma = ClassMethodActor.fromJava(Classes.getDeclaredMethod(Spinner.class, "run"));
            tm = cma.makeTargetMethod(Nature.OPT);
        }
        s.start();
        Thread.sleep(100);
        if (isMaxine) {
            new Deoptimization(new ArrayList<TargetMethod>(Arrays.asList(tm))).go();
        }
        Thread.sleep(100);
        System.out.println("done.");
        System.exit(0);
    }


    static class Spinner extends Thread {
        public Spinner() {
            super("Spinner");
        }
        int counter;
        @Override
        public void run() {
            while (true) {
                counter++;
            }
        }
    }
}
