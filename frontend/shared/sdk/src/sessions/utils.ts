import type { Session, SessionDTO } from '@insight/types';

export const mapSession = (sessionDTO: SessionDTO | Session): Session => {
  return { ...sessionDTO, createdAt: new Date(sessionDTO.createdAt) };
};
