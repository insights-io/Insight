export const getPublicApiBaseUrlEnvKey = (service: string) => {
  return `NEXT_PUBLIC_${service.toUpperCase()}_API_BASE_URL`;
};
