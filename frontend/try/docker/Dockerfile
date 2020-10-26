FROM node:14-alpine as build

WORKDIR /src

ARG auth_api_base_url=http://localhost:8080	
ENV NEXT_PUBLIC_AUTH_API_BASE_URL=$auth_api_base_url	

ARG app_base_url=http://localhost:3000	
ENV APP_BASE_URL=$app_base_url

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/try/package.json /src/frontend/try/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
COPY frontend/shared/sdk/package.json /src/frontend/shared/sdk/package.json
COPY frontend/shared/service-proxy/package.json /src/frontend/shared/service-proxy/package.json
COPY frontend/shared/testing/package.json /src/frontend/shared/testing/package.json
COPY frontend/shared/next-testing/package.json /src/frontend/shared/next-testing/package.json
COPY frontend/shared/next-storybook/package.json /src/frontend/shared/next-storybook/package.json
COPY frontend/shared/storybook/package.json /src/frontend/shared/storybook/package.json
COPY frontend/shared/elements/package.json /src/frontend/shared/elements/package.json
RUN yarn install --frozen-lockfile

COPY frontend/try/ /src/frontend/try/
COPY frontend/shared/ /src/frontend/shared/

RUN yarn workspace @insight/try build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/try

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/try/node_modules

COPY --from=build /src/frontend/try/package.json /usr/try/package.json
COPY --from=build /src/frontend/try/.next /usr/try/.next

COPY --from=build /src/frontend/shared/sdk/package.json /usr/try/node_modules/@insight/sdk/package.json
COPY --from=build /src/frontend/shared/sdk/dist /usr/try/node_modules/@insight/sdk/dist
COPY --from=build /src/frontend/shared/elements/package.json /usr/try/node_modules/@insight/elements/package.json
COPY --from=build /src/frontend/shared/elements/dist /usr/try/node_modules/@insight/elements/dist

EXPOSE 3000

CMD ["npm", "start"]
