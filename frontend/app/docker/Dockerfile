FROM node:14-alpine as build

WORKDIR /src

COPY . /src

RUN yarn install --frozen-lockfile
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
COPY --from=build /src/frontend/shared/elements/package.json /usr/app/node_modules/@insight/elements/package.json
COPY --from=build /src/frontend/shared/elements/dist /usr/app/node_modules/@insight/elements/dist

EXPOSE 3000

CMD ["npm", "start"]
