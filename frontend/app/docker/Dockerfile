FROM node:14-alpine as build

WORKDIR /src

ARG auth_api_client_base_url=http://localhost:8080
ENV NEXT_PUBLIC_AUTH_API_BASE_URL=$auth_api_client_base_url

ARG auth_api_server_base_url=http://localhost:8080
ENV AUTH_API_BASE_URL=$auth_api_server_base_url

ARG session_api_server_base_url=http://localhost:8082
ENV SESSION_API_BASE_URL=$session_api_server_base_url

ARG session_api_client_base_url=http://localhost:8082
ENV NEXT_PUBLIC_SESSION_API_BASE_URL=$session_api_client_base_url

ARG try_app_base_url=http://localhost:3002
ENV NEXT_PUBLIC_TRY_BASE_URL=$try_app_base_url

ARG bootstrap_script=https://static.dev.snuderls.eu/b/local.insight.js
ENV BOOTSTRAP_SCRIPT=$bootstrap_script

ARG jaeger_agent_host=jaeger
ENV JAEGER_AGENT_HOST=$jaeger_agent_host

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/app/package.json /src/frontend/app/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
COPY frontend/shared/sdk/package.json /src/frontend/shared/sdk/package.json
COPY frontend/shared/service-proxy/package.json /src/frontend/shared/service-proxy/package.json
COPY frontend/shared/testing/package.json /src/frontend/shared/testing/package.json
COPY frontend/shared/storybook/package.json /src/frontend/shared/storybook/package.json
COPY frontend/shared/ui/package.json /src/frontend/shared/ui/package.json
RUN yarn install --frozen-lockfile

COPY frontend/app/ /src/frontend/app/
COPY frontend/shared/ /src/frontend/shared/

RUN yarn frontend:app:build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/app

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/app/node_modules

COPY --from=build /src/frontend/app/package.json /usr/app/package.json
COPY --from=build /src/frontend/app/.next /usr/app/.next
COPY --from=build /src/frontend/app/public /usr/app/public

COPY --from=build /src/frontend/shared/sdk/package.json /usr/app/node_modules/@insight/sdk/package.json
COPY --from=build /src/frontend/shared/sdk/dist /usr/app/node_modules/@insight/sdk/dist
COPY --from=build /src/frontend/shared/ui/package.json /usr/app/node_modules/@insight/ui/package.json
COPY --from=build /src/frontend/shared/ui/dist /usr/app/node_modules/@insight/ui/dist

CMD ["npm", "start"]
