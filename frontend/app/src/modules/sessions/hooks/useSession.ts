import useSessions from './useSessions';

const useSession = (sessionId: string) => {
  const { data, loading } = useSessions();
  const maybeSession = data.find((session) => session.id === sessionId);

  return { session: maybeSession, loading };
};

export default useSession;
