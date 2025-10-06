# Stage 1: Build the React application (this is handled by Jenkins 'Build Website' stage)
# We will assume the 'build' directory is already present from the previous stage.

# Stage 2: Serve the built application with Nginx
FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY /build /usr/share/nginx/html