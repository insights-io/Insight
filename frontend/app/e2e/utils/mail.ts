/* eslint-disable no-console */
import { join } from 'path';
import { readFileSync, existsSync } from 'fs';
import { execSync } from 'child_process';

const linkPattern = /.*href="(http:\/\/.*)".*$/;

const projectRootPath = () => {
  return join(process.cwd(), '..', '..');
};

const getDockerLogPath = (): string => {
  let basePath = projectRootPath();

  if (process.env.ARTIFACTS_PATH) {
    basePath = join(basePath, process.env.ARTIFACTS_PATH);
  }

  return join(basePath, 'docker.log');
};

const readLinesFromDockerComposeLogs = (yamlPath: string): string[] => {
  return String(
    execSync(`docker-compose -f ${yamlPath} logs`, {
      maxBuffer: 500 * 1024 * 1024,
    })
  ).split('\n');
};

const getDockerComposeFilePath = () => {
  const basePath = projectRootPath();
  return join(basePath, 'backend', 'local-test-services', 'docker-compose.yml');
};

export const getDockerLogs = (): string[] => {
  const dockerLogPath = getDockerLogPath();
  if (existsSync(dockerLogPath)) {
    console.debug(
      `[TEST-SETUP]: Reading docker logs from file path=${dockerLogPath}`
    );
    return String(readFileSync(dockerLogPath)).split('\n');
  }

  const dockerComposeFilePath = getDockerComposeFilePath();
  console.debug(
    `[TEST-SETUP]: Unable to find docker log file path=${dockerLogPath}... Reading logs through docker-compose path=${dockerComposeFilePath}`
  );
  return readLinesFromDockerComposeLogs(dockerComposeFilePath);
};

export const findPatternInDockerLogs = (pattern: RegExp) => {
  const logs = getDockerLogs();

  return logs.reduce((maybeLink, line) => {
    const match = pattern.exec(line);
    if (match) {
      return match[1];
    }
    return maybeLink;
  }, undefined as string | undefined);
};

export const findLinkFromDockerLogs = () => {
  return findPatternInDockerLogs(linkPattern);
};
