# Use lightweight, safe alpine-based LTS Node image
FROM node:20-slim as builder

WORKDIR /usr/src/app

COPY package*.json tsconfig.json ./

# Install development + production dependencies
RUN npm ci

# Copy full source and build
COPY src ./src
RUN npm run build

# Remove development dependencies to keep final container small
RUN npm prune --production

# Stage 2: Final runtime image
FROM node:20-slim

WORKDIR /usr/src/app

COPY package*.json ./
COPY --from=builder /usr/src/app/node_modules ./node_modules
COPY --from=builder /usr/src/app/dist ./dist

ENV NODE_ENV=production
ENV PORT=8080

EXPOSE 8080

CMD ["npm", "start"]
