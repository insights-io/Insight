import React from 'react';
import { sandbox } from '@rebrowse/testing';
import { render } from 'test/utils';

import { Base } from './NetworkTab.stories';

describe('<NetworkTabContainer />', () => {
  it('Should render fetch events', async () => {
    const searchStub = Base.story.setupMocks(sandbox);
    const { findByText, findAllByText } = render(<Base />);

    await findByText(
      'beat?organizationId=000000&sessionId=d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba&deviceId=1978361a-dfae-4801-8d84-89dd6af21740&pageVisitId=032ba89d-0d8b-4f4e-b60f-516f8291e739'
    );
    await findByText('sessions');
    await findByText('d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba');
    await findAllByText('http/1.1');
    await findAllByText('fetch');
    await findAllByText('xmlhttprequest');

    sandbox.assert.calledWithExactly(searchStub, '10', {
      search: { 'event.e': ['eq:11'], limit: 1000 },
    });
  });
});
