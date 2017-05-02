package org.apache.cayenne.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GreetingTask extends DefaultTask {
    String greeting = 'Gradle Test...'

    @TaskAction
    def greet() {
        println greeting
    }
}
