FROM node:14-alpine as build

WORKDIR /src

ARG auth_api_client_base_url=http://localhost:8080
ENV NEXT_PUBLIC_AUTH_API_BASE_URL=$auth_api_client_base_url

ARG auth_api_server_base_url=http://localhost:8080
ENV AUTH_API_BASE_URL=$auth_api_server_base_url

COPY . /src

RUN yarn install --frozen-lockfile
RUN yarn workspace @insight/app build

FROM node:14-alpine

WORKDIR /usr/app

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/app/node_modules
COPY --from=build /src/frontend/app/package.json /usr/app/package.json
COPY --from=build /src/frontend/app/.next /usr/app/.next

CMD ["npm", "start"]
