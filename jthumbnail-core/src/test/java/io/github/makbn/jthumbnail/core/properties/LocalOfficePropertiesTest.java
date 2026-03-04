package io.github.makbn.jthumbnail.core.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

class LocalOfficePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "jthumbnailer.openoffice.manager_type=local",
                    "jthumbnailer.openoffice.hostname=127.0.0.1",
                    "jthumbnailer.openoffice.ports=2002",
                    "jthumbnailer.openoffice.pipe-names=jt-pipe",
                    "jthumbnailer.openoffice.working-dir=/tmp/jthumb-local-test");

    @Test
    void managerTypeIsLocal() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertEquals(ManagerType.LOCAL, p.managerType());
        });
    }

    @Test
    void hostnameBound() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertEquals("127.0.0.1", p.hostname());
        });
    }

    @Test
    void portsBound() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertNotNull(p.ports());
            assertEquals(1, p.ports().size());
            assertEquals(List.of(2002), p.ports());
        });
    }

    @Test
    void pipeNamesBound() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertNotNull(p.pipeNames());
            assertEquals(1, p.pipeNames().size());
            assertEquals("jt-pipe", p.pipeNames().get(0));
        });
    }

    @Test
    void workingDirBound() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertNotNull(p.workingDir());
            assertTrue(
                    p.workingDir().getPath().contains("jthumb-local-test"),
                    "workingDir should contain jthumb-local-test");
        });
    }

    @Test
    void officeHomeCanBeNull() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertNull(p.officeHome());
        });
    }

    @Test
    void defaultsApplied() {
        runner.run(ctx -> {
            LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
            assertFalse(p.failFast());
            assertEquals(120000L, p.connectionTimeout());
            assertEquals(1000, p.maxTasksPerConnection());
            assertEquals(30000L, p.taskQueueTimeout());
            assertEquals(120000L, p.taskExecutionTimeout());
            assertEquals(120000L, p.processTimeout());
        });
    }

    @Test
    void multiplePortsBound() {
        new ApplicationContextRunner()
                .withUserConfiguration(Config.class)
                .withPropertyValues(
                        "jthumbnailer.openoffice.manager_type=local",
                        "jthumbnailer.openoffice.ports=2002,2003",
                        "jthumbnailer.openoffice.working-dir=/tmp/jthumb-local-test")
                .run(ctx -> {
                    LocalOfficeProperties p = ctx.getBean(LocalOfficeProperties.class);
                    assertNotNull(p.ports());
                    assertEquals(2, p.ports().size());
                });
    }

    @Configuration
    @EnableConfigurationProperties(LocalOfficeProperties.class)
    static class Config {}
}
