/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;

/**
 * Detects Builders that do are not compatible with pipeline
 *
 * @author Alex Johnson
 */
public class PipelineIncompatibleBuildStepDetector extends BytecodeScanningDetector {

    private BugReporter reporter;

    public PipelineIncompatibleBuildStepDetector(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visit(JavaClass visitedClass){
        try {

            if (Repository.instanceOf(visitedClass, "hudson.tasks.Builder") ||
                    Repository.instanceOf(visitedClass, "hudson.tasks.Publisher")) {

                if (!Repository.implementationOf(visitedClass, "jenkins.tasks.SimpleBuildStep")) {
                    reporter.reportBug(new BugInstance(this, "PIPELINE_INCOMPATIBLE_BUILD_STEP",
                            NORMAL_PRIORITY).addClass(visitedClass.getClassName()));
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.visit(visitedClass);
    }

}
