export const getProxiedPublicApiBaseUrlEnvKey = (service: string) => {
  return `NEXT_PUBLIC_${service.toUpperCase()}_API_PROXIED_BASE_URL`;
};
