/*
 * The MIT License
 *
 * Copyright (c) 2016 Oleg Nenashev.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.lib.findbugs.detectors;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import static edu.umd.cs.findbugs.Priorities.NORMAL_PRIORITY;
import edu.umd.cs.findbugs.ba.ObjectTypeFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ObjectType;

/**
 * Detects usages of closeQuietly().
 * We do not want to allow them since the errors are not being logged.
 * @author Oleg Nenashev
 */
public class CloseQuietlyInsteadOfLoggingDetector extends BytecodeScanningDetector {
    
    /** The reporter to report to */
    private final BugReporter reporter;

    /** Name of the class being inspected */
    private String currentClass;

    /** Jenkins type */
    private final ObjectType commonsIOUtils = ObjectTypeFactory.getInstance("");
    private final ObjectType hudsonIOUtils = ObjectTypeFactory.getInstance("");
    
    private static final HashSet<String> CLOSE_QUIETLY_CLASSES = new HashSet<>(Arrays.asList(
            "hudson/util/IOUtils",
            "org/apache/commons/io/IOUtils"
    ));
            
    
    Method currentMethod = null;
    
    public CloseQuietlyInsteadOfLoggingDetector(BugReporter reporter) {
        this.reporter = reporter;
    }
    
    /**
     * Records the class name for the future use.
     * @param visitedClass Class, which is being visited
     */
    @Override
    public void visit(JavaClass visitedClass) {
        currentClass = visitedClass.getClassName();
        super.visit(visitedClass);
    }

    @Override
    public void visitMethod(Method obj) {
        this.currentMethod = obj;
        super.visitMethod(obj);
    }
    
    

    @Override
    public void sawField() {
        super.sawField(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void sawOpcode(int seen) {
        // we are only interested in method calls
        if (seen != INVOKESTATIC || !isMethodCall()) {
            return;
        }

        System.out.println("RES: " + getClassConstantOperand() + " - " + getMethodDescriptorOperand().getName());
        if (getMethodDescriptorOperand().getName().contains("closeQuietly") && 
                CLOSE_QUIETLY_CLASSES.contains(getClassConstantOperand())) {
            System.out.println(">>> match!");
            // Found closeQuietly() call
            reporter.reportBug(new BugInstance(this, "CLOSE_QUIETLY_NO_LOGGING", NORMAL_PRIORITY)
                    .addClass(currentClass)
                    .addMethod(getMethodDescriptor())
                    .addSourceLine(this));
        }
    }
}
