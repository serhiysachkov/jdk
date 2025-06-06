/*
 * Copyright (c) 2002, 2025, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.ObjectReference.setValue;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Field;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.NativeMethodException;

import java.util.Iterator;
import java.util.List;
import java.io.*;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

/**
 * The test checks that the JDI method
 * <code>com.sun.jdi.ObjectReference.setValue()</code>
 * does not throw <i>ClassNotLoadedException</i> when a
 * debugger part of the test attempts to set null value of
 * a debuggee field of reference type which has not been loaded
 * through the appropriate class loader.<br>
 */
public class setvalue005 {
    static final String DEBUGGEE_CLASS =
        "nsk.jdi.ObjectReference.setValue.setvalue005t";

    // tested debuggee reference types
    static final int REFTYPES_NUM = 3;
    static final String DEBUGGEE_REFTYPES[][] = {
        {"nsk.jdi.ObjectReference.setValue.setvalue005tDummyType", "dummyType"},
        {"nsk.jdi.ObjectReference.setValue.setvalue005tAbsDummyType", "absDummyType"},
        {"nsk.jdi.ObjectReference.setValue.setvalue005tFinDummyType", "finDummyType"}
    };

    // name of debuggee main thread
    static final String DEBUGGEE_THRNAME = "setvalue005tThr";

    // debuggee local var used to find needed stack frame
    static final String DEBUGGEE_LOCALVAR = "setvalue005tPipe";

    static final int ATTEMPTS = 5;

    static final String COMMAND_READY = "ready";
    static final String COMMAND_QUIT = "quit";

    private Log log;
    private IOPipe pipe;
    private Debugee debuggee;
    private ThreadReference thrRef = null;
    private int tot_res = Consts.TEST_PASSED;

    public static void main (String argv[]) {
        int result = run(argv,System.out);
        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run(String argv[], PrintStream out) {
        return new setvalue005().runIt(argv, out);
    }

    private int runIt(String args[], PrintStream out) {
        ArgumentHandler argHandler = new ArgumentHandler(args);
        log = new Log(out, argHandler);
        Binder binder = new Binder(argHandler, log);
        ObjectReference objRef;
        ReferenceType rType;
        String cmd;
        int num = 0;

        debuggee = binder.bindToDebugee(DEBUGGEE_CLASS);
        pipe = debuggee.createIOPipe();
        debuggee.redirectStderr(log, "setvalue005t.err> ");
        debuggee.resume();
        cmd = pipe.readln();
        if (!cmd.equals(COMMAND_READY)) {
            log.complain("TEST BUG: unknown debuggee command: " + cmd);
            tot_res = Consts.TEST_FAILED;
            return quitDebuggee();
        }

        ReferenceType debuggeeClass = debuggee.classByName(DEBUGGEE_CLASS); // debuggee main class

        thrRef = debuggee.threadByFieldName(debuggeeClass, "testThread", DEBUGGEE_THRNAME);
        if (thrRef == null) {
            log.complain("TEST FAILURE: Method Debugee.threadByFieldName() returned null for debuggee thread "
                + DEBUGGEE_THRNAME);
            tot_res = Consts.TEST_FAILED;
            return quitDebuggee();
        }
        thrRef.suspend();
        while(!thrRef.isSuspended()) {
            num++;
            if (num > ATTEMPTS) {
                log.complain("TEST FAILED: Unable to suspend debuggee thread after "
                    + ATTEMPTS + " attempts");
                tot_res = Consts.TEST_FAILED;
                return quitDebuggee();
            }
            log.display("Waiting for debuggee thread suspension ...");
            try {
                Thread.currentThread().sleep(1000);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
                log.complain("TEST FAILED: caught: " + ie);
                tot_res = Consts.TEST_FAILED;
                return quitDebuggee();
            }
        }

// Check the tested assersion
        try {
            objRef = findObjRef(DEBUGGEE_LOCALVAR);
            rType = objRef.referenceType();

            // provoke the ClassNotLoadedException
            for (int i=0; i<REFTYPES_NUM; i++) {
                Field fld = rType.fieldByName(DEBUGGEE_REFTYPES[i][1]);
                if (fld==null) {
                    log.complain("TEST FAILURE: the expected debuggee field \""
                        + DEBUGGEE_REFTYPES[i][1]
                        + "\" not found through the JDI method ReferenceType.fieldByName()");
                    tot_res = Consts.TEST_FAILED;
                    continue;
                }

                // Make sure that the reference type is not loaded by the debuggee VM
                if ((debuggee.classByName(DEBUGGEE_REFTYPES[i][0])) != null) {
                    log.display("\nSkipping the check: the tested reference type\n\t\""
                        + DEBUGGEE_REFTYPES[i][0]
                        + "\"\n\twas loaded by the debuggee VM, unable to test an assertion");
                    continue;
                }

                log.display("\nTrying to set null value for the field \""
                    + fld.name() + "\"\n\tof non-loaded reference type \""
                    + fld.typeName()
                    + "\"\n\tgot from the debuggee object reference \""
                    + objRef + "\" ...");

                try {
                    objRef.setValue(fld, null
                        /*objRef.getValue(rType.fieldByName(fld.name()))*/);

                    log.display("CHECK PASSED: ClassNotLoadedException was not thrown as expected"
                        + "\n\twhen attempted to set null value for the field \""
                        + fld.name() + "\"\n\tof non-loaded reference type \""
                        + fld.typeName()
                        + "\"\n\tgot from the debuggee object reference \""
                        + objRef + "\"");
                } catch (ClassNotLoadedException ce) {
                    log.complain("TEST FAILED: " + ce + " was thrown"
                        + "\n\twhen attempted to set null value for the field \""
                        + fld.name() + "\"\n\tof non-loaded reference type \""
                        + fld.typeName()
                        + "\"\n\tgot from the debuggee object reference \""
                        + objRef + "\"");
                    tot_res = Consts.TEST_FAILED;
                } catch (Exception ue) {
                    ue.printStackTrace();
                    log.complain("TEST FAILED: ObjectReference.setValue(): caught unexpected "
                        + ue + "\n\twhen attempted to set null value for the field \""
                        + fld.name() + "\"\n\tof reference type \"" + fld.typeName()
                        + "\"\n\tgot from the debuggee object reference \""
                        + objRef + "\"");
                    tot_res = Consts.TEST_FAILED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.complain("TEST FAILURE: caught unexpected exception: " + e);
            tot_res = Consts.TEST_FAILED;
            return quitDebuggee();
        }

// Finish the test
        return quitDebuggee();
    }

    private ObjectReference findObjRef(String varName) {
        try {
            List frames = thrRef.frames();
            Iterator iter = frames.iterator();
            while (iter.hasNext()) {
                StackFrame stackFr = (StackFrame) iter.next();
                try {
                    LocalVariable locVar = stackFr.visibleVariableByName(varName);

                    if (locVar == null) continue;

                    // return reference to the debuggee class' object
                    return stackFr.thisObject();
                } catch(AbsentInformationException e) {
                    // this is not needed stack frame, ignoring
                } catch(NativeMethodException ne) {
                    // current method is native, also ignoring
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            tot_res = Consts.TEST_FAILED;
            throw new Failure("findObjRef: caught unexpected exception: " + e);
        }
        throw new Failure("findObjRef: needed debuggee stack frame not found");
    }

    private int quitDebuggee() {
        if (thrRef != null) {
            if (thrRef.isSuspended())
                thrRef.resume();
        }
        pipe.println(COMMAND_QUIT);
        debuggee.waitFor();
        int debStat = debuggee.getStatus();
        if (debStat != (Consts.JCK_STATUS_BASE + Consts.TEST_PASSED)) {
            log.complain("TEST FAILED: debuggee process finished with status: "
                + debStat);
            tot_res = Consts.TEST_FAILED;
        } else
            log.display("\nDebuggee process finished with the status: "
                + debStat);

        return tot_res;
    }

}
