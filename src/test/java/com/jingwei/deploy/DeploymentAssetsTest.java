package com.jingwei.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeploymentAssetsTest {

    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();

    @Test
    void systemdServiceUsesDedicatedWritableTempDirectory() throws IOException {
        String service = Files.readString(PROJECT_ROOT.resolve("deploy/jingwei.service"));

        assertThat(service).contains("-Djava.io.tmpdir=/opt/jingwei/tmp");
        assertThat(service).contains("ReadWritePaths=/opt/jingwei/logs /opt/jingwei/tmp");
    }

    @Test
    void deployScriptSecuresRuntimeDirectoriesAndEnvFile() throws IOException {
        String script = Files.readString(PROJECT_ROOT.resolve("deploy/deploy.sh"));

        assertThat(script).contains("mkdir -p \"${DEPLOY_DIR}/logs\" \"${DEPLOY_DIR}/tmp\"");
        assertThat(script).contains("chown -R \"${APP_USER}:${APP_GROUP}\" \"${DEPLOY_DIR}\"");
        assertThat(script).contains("chown root:\"${APP_GROUP}\" \"${DEPLOY_DIR}/.env\"");
        assertThat(script).contains("chmod 640 \"${DEPLOY_DIR}/.env\"");
    }

    @Test
    void productionConfigCanBeDrivenByEnvironmentVariables() throws IOException {
        String prodConfig = Files.readString(PROJECT_ROOT.resolve("src/main/resources/application-prod.yml"));
        String envExample = Files.readString(PROJECT_ROOT.resolve("deploy/.env.example"));

        assertThat(prodConfig).contains("jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:jingwei}");
        assertThat(prodConfig).contains("username: ${DB_USERNAME:jingwei}");
        assertThat(envExample).contains("DB_HOST=localhost");
        assertThat(envExample).contains("DB_USERNAME=jingwei");
        assertThat(envExample).contains("JWT_SECRET=");
    }

    @Test
    void runbookMatchesProductionRuntimeConfiguration() throws IOException {
        String runbook = Files.readString(PROJECT_ROOT.resolve("deploy/RUNBOOK.md"));

        assertThat(runbook).contains("-Djava.io.tmpdir=/opt/jingwei/tmp");
        assertThat(runbook).contains("`DB_HOST`");
        assertThat(runbook).contains("`DB_PORT`");
        assertThat(runbook).contains("`DB_NAME`");
        assertThat(runbook).contains("`DB_USERNAME`");
        assertThat(runbook).contains("curl http://localhost:8080/api/actuator/health");
        assertThat(runbook).contains("/opt/jingwei/logs/jingwei.log");
    }
}
