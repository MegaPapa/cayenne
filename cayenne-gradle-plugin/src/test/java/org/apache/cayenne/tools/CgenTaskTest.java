package org.apache.cayenne.tools;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/**
 * @since 4.0
 */
public class CgenTaskTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setUp() throws Exception {
        buildFile = temp.newFile("build.gradle");
    }

    @Test
    public void generateTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("org.apache.cayenne");



        //project.getTasks().getByName("cgen").
    }

    private Project createProject(String projectName) {
        try {
            File projectDir = this.temp.newFolder(projectName);
            return ProjectBuilder.builder().withProjectDir(projectDir)
                    .withName(projectName).build();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private CgenTask createTask(Project project) {
        return project.getTasks().create("testCgenTask", CgenTask.class);
    }

}