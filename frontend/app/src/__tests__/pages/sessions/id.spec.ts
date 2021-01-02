import { TermCondition } from '@rebrowse/sdk';
import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import type { AutoSizerProps } from 'react-virtualized-auto-sizer';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { sessionDescription } from 'sessions/utils';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import {
  REBROWSE_SESSIONS,
  REBROWSE_SESSIONS_DTOS,
} from '__tests__/data/sessions';
import { mockSessionPage, mockSessionsPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

describe('/sessions/[id]', () => {
  /* Data */
  const route = `${SESSIONS_PAGE}/random`;

  test('As a user I should be redirected to /sessions on 404 request', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub } = mockSessionsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveSessionStub, 'random', {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    await screen.findAllByText('Mac OS X • Chrome');
  });

  test('As a user I should be able to work with Developer tools', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub, searchEventsStub } = mockSessionPage(sandbox);
    const [{ id }] = REBROWSE_SESSIONS_DTOS;

    /* Server */
    const { page } = await getPage({ route: `${SESSIONS_PAGE}/${id}` });

    sandbox.assert.calledWithExactly(retrieveSessionStub, id, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    userEvent.click(screen.getByLabelText('Developer tools'));

    // Console events
    await screen.findByText(
      `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0. '|' and '.' will no longer create a hierarchy, but codemods are available. Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`
    );
    expect(screen.getByText('[Fast Refresh] done')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    userEvent.click(
      screen.getByText('Uncaught SyntaxError: Unexpected identifier')
    );
    expect(
      screen.getByText('at eval (__playwright_evaluation_script__45:11:47)', {
        exact: false,
      })
    ).toBeInTheDocument();

    sandbox.assert.calledWithExactly(searchEventsStub, id, {
      search: {
        'event.e': [TermCondition.GTE(9), TermCondition.LTE(10)],
        limit: 1000,
      },
      ...INCLUDE_CREDENTIALS,
    });

    userEvent.click(screen.getByText('Network'));

    // Fetch events
    await screen.findByText(
      'beat?organizationId=000000&sessionId=d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba&deviceId=1978361a-dfae-4801-8d84-89dd6af21740&pageVisitId=032ba89d-0d8b-4f4e-b60f-516f8291e739'
    );

    sandbox.assert.calledWithExactly(searchEventsStub, id, {
      search: { 'event.e': [TermCondition.EQ(11)], limit: 1000 },
      ...INCLUDE_CREDENTIALS,
    });
  });

  test('As a user I can search for "similar sessions" by tags', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      retrieveSessionStub,
      listSessionsStub,
      countSessionsStub,
    } = mockSessionsPage(sandbox);

    const [session] = REBROWSE_SESSIONS;

    const {
      id,
      userAgent: {
        deviceClass,
        deviceBrand,
        deviceName,
        agentName,
        agentVersion,
        operatingSystemName,
        operatingSystemVersion,
      },
      location: { ip, continentName, countryName, regionName, city },
    } = session;

    /* Server */
    const { page } = await getPage({ route: `${SESSIONS_PAGE}/${id}` });

    sandbox.assert.calledWithExactly(retrieveSessionStub, id, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    /* user_agent.device_class */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`device.class = ${deviceClass}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(deviceClass))).toBeInTheDocument();
    expect(screen.getByText('Device class')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.device_class': TermCondition.EQ(deviceClass),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'user_agent.device_class': TermCondition.EQ(deviceClass) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* user_agent.device_brand */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`device.brand = ${deviceBrand}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(deviceBrand))).toBeInTheDocument();
    expect(screen.getByText('Device brand')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.device_brand': TermCondition.EQ(deviceBrand),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'user_agent.device_brand': TermCondition.EQ(deviceBrand) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* user_agent.device_name */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`device.name = ${deviceName}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(deviceName))).toBeInTheDocument();
    expect(screen.getByText('Device name')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.device_name': TermCondition.EQ(deviceName),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'user_agent.device_name': TermCondition.EQ(deviceName) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* user_agent.agent_name & user_agent.agent_version */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`browser = ${agentName} ${agentVersion}`));

    await screen.findByText('2 Filters');
    expect(screen.getByText(TermCondition.EQ(agentName))).toBeInTheDocument();
    expect(
      screen.getByText(TermCondition.EQ(agentVersion))
    ).toBeInTheDocument();
    expect(screen.getByText('Browser name')).toBeInTheDocument();
    expect(screen.getByText('Browser version')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.agent_name': TermCondition.EQ(agentName),
        'user_agent.agent_version': TermCondition.EQ(agentVersion),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        'user_agent.agent_name': TermCondition.EQ(agentName),
        'user_agent.agent_version': TermCondition.EQ(agentVersion),
      },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* user_agent.agent_name  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`browser.name = ${agentName}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(agentName))).toBeInTheDocument();
    expect(screen.getByText('Browser name')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.agent_name': TermCondition.EQ(agentName),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        'user_agent.agent_name': TermCondition.EQ(agentName),
      },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* user_agent.operating_system_name & user_agent.operating_system_version  */
    await screen.findByText(id);
    userEvent.click(
      screen.getByText(
        `client_os = ${operatingSystemName} ${operatingSystemVersion}`
      )
    );

    await screen.findByText('2 Filters');
    expect(
      screen.getByText(TermCondition.EQ(operatingSystemName))
    ).toBeInTheDocument();
    expect(
      screen.getByText(TermCondition.EQ(operatingSystemVersion))
    ).toBeInTheDocument();
    expect(screen.getByText('Operating System name')).toBeInTheDocument();
    expect(screen.getByText('Operating System version')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'user_agent.operating_system_name': TermCondition.EQ(
          operatingSystemName
        ),
        'user_agent.operating_system_version': TermCondition.EQ(
          operatingSystemVersion
        ),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        'user_agent.operating_system_name': TermCondition.EQ(
          operatingSystemName
        ),
        'user_agent.operating_system_version': TermCondition.EQ(
          operatingSystemVersion
        ),
      },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* location.ip  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`user.ip = ${ip}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(ip))).toBeInTheDocument();
    expect(screen.getByText('IP address')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'location.ip': TermCondition.EQ(ip),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'location.ip': TermCondition.EQ(ip) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* location.continent_name  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`user.continent = ${continentName}`));

    await screen.findByText('1 Filters');
    expect(
      screen.getByText(TermCondition.EQ(continentName))
    ).toBeInTheDocument();
    expect(screen.getByText('Continent')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'location.continent_name': TermCondition.EQ(continentName),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'location.continent_name': TermCondition.EQ(continentName) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* location.country_name  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`user.country = ${countryName}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(countryName))).toBeInTheDocument();
    expect(screen.getByText('Country')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'location.country_name': TermCondition.EQ(countryName),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'location.country_name': TermCondition.EQ(countryName) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* location.region_name  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`user.region = ${regionName}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(regionName))).toBeInTheDocument();
    expect(screen.getByText('State/Region')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'location.region_name': TermCondition.EQ(regionName),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'location.region_name': TermCondition.EQ(regionName) },
    });
    userEvent.click(screen.getAllByText(sessionDescription(session))[0]);

    /* location.city  */
    await screen.findByText(id);
    userEvent.click(screen.getByText(`user.city = ${city}`));

    await screen.findByText('1 Filters');
    expect(screen.getByText(TermCondition.EQ(city))).toBeInTheDocument();
    expect(screen.getByText('City')).toBeInTheDocument();

    sandbox.assert.calledWithExactly(listSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        limit: 20,
        sortBy: ['-createdAt'],
        'location.city': TermCondition.EQ(city),
      },
    });
    sandbox.assert.calledWithExactly(countSessionsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: { 'location.city': TermCondition.EQ(city) },
    });
  });
});
