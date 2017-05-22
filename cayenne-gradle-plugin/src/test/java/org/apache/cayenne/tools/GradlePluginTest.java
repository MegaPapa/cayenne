package org.apache.cayenne.tools;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since 4.0
 */
public class GradlePluginTest {

    @Test
    public void generateTest() throws Exception {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("org.apache.cayenne");

        assertTrue(project.getTasks().getByName("cgen") instanceof CgenTask);
        assertTrue(project.getTasks().getByName("cdbimport") instanceof DbImportTask);
        assertTrue(project.getTasks().getByName("cdbgen") instanceof DbGenerateTask);

        assertTrue(project.getExtensions().getByName("cayenne") instanceof GradleCayenneExtension);

    }
}
