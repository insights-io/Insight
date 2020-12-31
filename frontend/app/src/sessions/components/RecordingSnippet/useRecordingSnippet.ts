import { useQuery } from 'shared/hooks/useQuery';
import { getBoostrapScript } from '@rebrowse/sdk';

const cacheKey = (snippetUri: string) => {
  return ['recordingSnippet', snippetUri];
};

export const useRecordingSnippet = (
  snippetUri: string,
  organizationId: string
) => {
  const { data } = useQuery(cacheKey(snippetUri), () =>
    getBoostrapScript(snippetUri).then((httpResponse) =>
      `<script>\n${httpResponse.data.trim()}\n</script>`.replace(
        '<ORG>',
        organizationId
      )
    )
  );

  return { data };
};
