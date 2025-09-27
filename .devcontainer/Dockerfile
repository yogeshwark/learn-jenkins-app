
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
# netlify-cli is now a dev dependency, installed via npm ci in Stage 1

# Define the command to run when the container starts
# CMD ["npm", "run", "dev"]
