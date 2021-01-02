import type { Input } from '@rebrowse/sdk';
import { client } from 'sdk';
import { useQuery } from 'shared/hooks/useQuery';

const cacheKey = (bootstrapScriptUrl: Input) => {
  return ['recordingSnippet', bootstrapScriptUrl];
};

export const useRecordingSnippet = (
  bootstrapScriptUrl: Input,
  organizationId: string
) => {
  const { data } = useQuery(cacheKey(bootstrapScriptUrl), () =>
    client.tracking
      .retrieveBoostrapScript(bootstrapScriptUrl)
      .then((httpResponse) =>
        `<script>\n${httpResponse.data.trim()}\n</script>`.replace(
          '<ORG>',
          organizationId
        )
      )
  );

  return { data };
};
