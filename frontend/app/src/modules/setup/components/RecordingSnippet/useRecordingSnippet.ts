import { useQuery } from 'shared/hooks/useQuery';
import ky from 'ky-universal';

export const cacheKey = (snippetUri: string) => {
  return ['recordingSnippet', snippetUri];
};

const queryFn = (snippetUri: string) => {
  return ky.get(snippetUri).text();
};

export const useRecordingSnippet = (
  snippetUri: string,
  organizationId: string
) => {
  const { data } = useQuery(cacheKey(snippetUri), () =>
    queryFn(snippetUri).then((text) =>
      `<script>\n${text.trim()}\n</script>`.replace('<ORG>', organizationId)
    )
  );

  return { data };
};
