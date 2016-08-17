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

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ObjectType;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ba.ObjectTypeFactory;


/**
 * Ensures that there is no static Jenkins field declaration.
 * Jenkins should not be used as a static field, because instance may change in particular cases.
 * @author Oleg Nenashev
 */
public class StaticJenkinsInstanceDetector extends BytecodeScanningDetector {

    /** The reporter to report to */
    private final BugReporter reporter;

    /** Name of the class being inspected */
    private String currentClass;

    /** Jenkins type */
    private final ObjectType jenkinsType = ObjectTypeFactory.getInstance("jenkins.model.Jenkins");

    public StaticJenkinsInstanceDetector(BugReporter reporter) {
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
    public void sawField() {
        super.sawField(); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Checks if the field is static Jenkins instance.
     * @param aField Field to be checked
     */
    @Override
    public void visit(Field aField) {
        
        super.visit(aField);
        if (!aField.isStatic()) return; // exit early on non-static fields
        Object fieldtype = aField.getType();
        if (!(fieldtype instanceof ObjectType)) return; // exit early on non-object fields
        
        boolean isJenkinsInstance = false;
        try {
            if (((ObjectType)fieldtype).subclassOf(jenkinsType)) {
                isJenkinsInstance = true;
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        if (isJenkinsInstance) {
            reporter.reportBug(new BugInstance(this, "STCAL_STATIC_JENKINS_INSTANCE", NORMAL_PRIORITY)
                    .addClass(currentClass)
                    .addField(currentClass, aField.getName(), aField.getSignature(), true));
        }
    }
}