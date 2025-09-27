pipeline {
    agent any

    environment {
        CUSTOM_DOCKER_IMAGE = "my-app-dev" // Define a tag for your custom image
    }

    stages {
        stage('Prepare Environment') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Building custom Docker image from .devcontainer/Dockerfile ---"
                        // Build the Docker image from the .devcontainer folder
                        sh '''
                            docker build -t "${CUSTOM_DOCKER_IMAGE}" -f .devcontainer/Dockerfile .
                        '''
                    } else {
                        echo "--- Skipping custom Docker image build on Windows host ---"
                        // For Windows, you might need a different strategy or skip this if devcontainers are Unix-specific
                        // Or, if Docker Desktop is available, you could run a bat command to build
                        // For simplicity, we'll assume Unix agent for custom Docker image build
                    }
                }
            }
        }
    }
}
