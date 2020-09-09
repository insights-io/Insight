import { AuthApi } from 'api';
import useSWR from 'swr';

const CACHE_KEY = 'AuthApi.organizations.get';

const useOrganization = () => {
  const { data: organization } = useSWR(CACHE_KEY, () =>
    AuthApi.organization.get()
  );

  const isLoading = organization === undefined;

  return { organization, isLoading };
};

export default useOrganization;
