package io.jenkins.lib.findbugs.samples.StaticJenkinsInstanceDetector;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import jenkins.model.Jenkins;

/**
 * Just a test class, which invokes StaticJenkinsInstanceDetector.
 * @author Oleg Nenashev
 */
@Extension
public class Sample1 extends QueueTaskDispatcher {
    
    private static final Jenkins JENKINS = Jenkins.getInstance();

    @Override
    public CauseOfBlockage canRun(Queue.Item item) {
        JENKINS.disableSecurity();
        return super.canRun(item);
    }
}
