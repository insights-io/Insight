import React, { useRef, useState } from 'react';
import useSWR from 'swr';
import ky from 'ky-universal';
import { Block, BlockOverrides } from 'baseui/block';
import { useStyletron } from 'baseui';
import { Spinner } from 'baseui/spinner';
import { H3, Paragraph3 } from 'baseui/typography';
import { Button, SIZE, SHAPE } from 'baseui/button';
import { Check } from 'baseui/icon';

type Props = {
  snipetURI: string;
  organizationId: string;
  overrides?: { Root?: BlockOverrides };
};

const RecordingSnippet = ({ snipetURI, organizationId, overrides }: Props) => {
  const [css, theme] = useStyletron();
  const codeRef = useRef<HTMLElement>(null);
  const [copied, setCopied] = useState(false);
  const { data: recordingSnippet } = useSWR('recordingSnippet', () =>
    ky
      .get(snipetURI)
      .text()
      .then((text) =>
        `<script>\n${text.trim()}\n</script>`.replace('<ORG>', organizationId)
      )
  );

  const onCopy = () => {
    if (!recordingSnippet) {
      return;
    }
    const textArea = document.createElement('textarea');
    textArea.textContent = recordingSnippet;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    setCopied(true);

    setTimeout(() => {
      setCopied(false);
    }, 2000);
  };

  return (
    <Block>
      <H3 $style={{ fontSize: '22px', lineHeight: '22px' }} marginTop={0}>
        Ready to get Insights? Setup the recording snippet.
      </H3>
      <Paragraph3>
        Paste your snippet into the{' '}
        <span className={css({ color: theme.colors.positive300 })}>
          &lt;head&gt;
        </span>{' '}
        of your website.
      </Paragraph3>
      <Block
        overrides={overrides?.Root}
        position="relative"
        backgroundColor={theme.colors.mono300}
        padding={theme.sizing.scale400}
        $style={{
          borderRadius: theme.sizing.scale500,
          textAlign: recordingSnippet === undefined ? 'center' : undefined,
        }}
      >
        <pre className={css({ margin: 0 })}>
          <code ref={codeRef}>{recordingSnippet || <Spinner />}</code>
        </pre>
        <Button
          size={SIZE.mini}
          shape={SHAPE.pill}
          $style={{
            position: 'absolute',
            top: theme.sizing.scale500,
            right: theme.sizing.scale500,
          }}
          onClick={onCopy}
        >
          {copied ? <Check /> : 'Copy'}
        </Button>
      </Block>
    </Block>
  );
};

export default RecordingSnippet;
