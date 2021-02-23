### BASE ###
FROM node:15.9.0-alpine as base

WORKDIR /src

### BUILDER ###
FROM base AS builder

# Install production dependencies
COPY package.json yarn.lock tsconfig.json lerna.json ./
COPY frontend/shared/sdk/package.json ./frontend/shared/sdk/package.json
COPY frontend/shared/service-proxy/package.json ./frontend/shared/service-proxy/package.json
COPY frontend/shared/elements/package.json ./frontend/shared/elements/package.json
COPY frontend/accounts/package.json ./frontend/accounts/package.json

RUN yarn install --production --frozen-lockfile --prefer-offline --ignore-scripts
RUN cp -RL ./node_modules/ /tmp/node_modules/

# Copy source files
COPY frontend/accounts/ ./frontend/accounts/
COPY frontend/shared/ ./frontend/shared/

# Install all dependencies
RUN yarn install --frozen-lockfile --prefer-offline

# Build
RUN yarn frontend:accounts:build

### RUNNER ###
FROM base

WORKDIR /usr/app

# Copy runtime dependencies
COPY --from=builder /tmp/node_modules/ ./node_modules/

COPY --from=builder /src/frontend/accounts/.next ./.next

COPY --from=builder /src/frontend/shared/types/package.json ./node_modules/@rebrowse/types/package.json
COPY --from=builder /src/frontend/shared/types/dist ./node_modules/@rebrowse/types/dist

COPY --from=builder /src/frontend/shared/sdk/package.json ./node_modules/@rebrowse/sdk/package.json
COPY --from=builder /src/frontend/shared/sdk/dist ./node_modules/@rebrowse/sdk/dist

COPY --from=builder /src/frontend/shared/elements/package.json ./node_modules/@rebrowse/elements/package.json
COPY --from=builder /src/frontend/shared/elements/dist ./node_modules/@rebrowse/elements/dist

USER node
ENV NODE_ENV production

EXPOSE 3000

CMD ["node", "node_modules/next/dist/bin/next", "start"]
