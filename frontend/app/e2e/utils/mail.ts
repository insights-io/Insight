import { join } from 'path';
import { readFileSync, existsSync } from 'fs';
import { execSync } from 'child_process';

const linkPattern = /.*href="(http:\/\/.*)".*$/;

const getDockerLogPath = (): string => {
  let basePath = join(process.cwd(), '..', '..');

  if (process.env.ARTIFACTS_PATH) {
    basePath = join(basePath, process.env.ARTIFACTS_PATH);
  }

  return join(basePath, 'docker.log');
};

const dockerLogFileExists = (): boolean => {
  return existsSync(getDockerLogPath());
};

const readLinesFromDockerLogFile = (): string[] => {
  return String(readFileSync(getDockerLogPath())).split('\n');
};

const readLinesFromDockerComposeLogs = (): string[] => {
  const localTestServicesPath = join(
    process.cwd(),
    '..',
    '..',
    'backend',
    'local-test-services'
  );

  return String(
    execSync(`cd ${localTestServicesPath} && docker-compose logs`)
  ).split('\n');
};

export const getDockerLogs = (): string[] => {
  return dockerLogFileExists()
    ? readLinesFromDockerLogFile()
    : readLinesFromDockerComposeLogs();
};

export const findLinkFromDockerLogs = () => {
  const logs = getDockerLogs();

  return logs.reduce((maybeLink, line) => {
    const match = linkPattern.exec(line);
    if (match) {
      return match[1];
    }
    return maybeLink;
  }, undefined as string | undefined);
};
