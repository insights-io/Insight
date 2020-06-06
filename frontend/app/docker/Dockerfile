FROM node:14-alpine as build

WORKDIR /src

ARG auth_api_client_base_url=http://localhost:8080
ENV NEXT_PUBLIC_AUTH_API_BASE_URL=$auth_api_client_base_url

ARG auth_api_server_base_url=http://localhost:8080
ENV AUTH_API_BASE_URL=$auth_api_server_base_url

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/app/package.json /src/frontend/app/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
COPY frontend/shared/testing/package.json /src/frontend/shared/testing/package.json
COPY frontend/shared/storybook/package.json /src/frontend/shared/storybook/package.json
RUN yarn install --frozen-lockfile

COPY frontend/app/ /src/frontend/app/
COPY frontend/shared/ /src/frontend/shared/

RUN yarn workspace @insight/app build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/app

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/app/node_modules
COPY --from=build /src/frontend/app/package.json /usr/app/package.json
COPY --from=build /src/frontend/app/.next /usr/app/.next

CMD ["npm", "start"]
