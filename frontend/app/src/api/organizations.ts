import ky from 'ky-universal';
import {
  DataResponse,
  UserDTO,
  OrganizationDTO,
  Organization,
} from '@insight/types';

import { authApiBaseURL } from './base';

export const mapOrganization = (
  organization: OrganizationDTO
): Organization => {
  return { ...organization, createdAt: new Date(organization.createdAt) };
};

const OrganizationsApi = {
  get: (baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/organizations`, { credentials: 'include' })
      .json<DataResponse<OrganizationDTO>>()
      .then((response) => mapOrganization(response.data));
  },
  members: (baseURL = authApiBaseURL) => {
    return ky
      .get(`${baseURL}/v1/organizations/members`, { credentials: 'include' })
      .json<DataResponse<UserDTO[]>>()
      .then((response) => response.data);
  },
};

export default OrganizationsApi;
