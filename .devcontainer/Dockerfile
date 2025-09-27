
# Stage 1: Build dependencies for caching
FROM node:20-alpine AS dependencies
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# Stage 2: Final development image
FROM node:20-alpine
WORKDIR /app
COPY --from=dependencies /app/node_modules ./node_modules
COPY . .
# Add any other dev tools, for example:
# RUN npm install -g typescript
RUN npm install serve
RUN npm install netlify-cli

# Define the command to run when the container starts
# CMD ["npm", "run", "dev"]
