pipeline {
    agent any

    stages {
        stage('Build and Test') {
            steps {
                script {
                    if (isUnix()) {
                        // This block runs on Linux agents using a Docker container
                        echo "--- Running build on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh '''
                                echo 'Preparing Build Environment'
                                node --version
                                npm --version
                                npm ci
                                echo 'Before build'
                                ls -la
                                npm run build
                                echo 'After build'
                                ls -la
                                
                                if [ ! -f "build/index.html" ]; then
                                    echo "index.html does not exist into build folder."
                                    exit 1
                                fi
                                echo "index.html exists into build folder."
                                npm test
                            '''
                        }
                    } else {
                        // This block runs on Windows agents directly on the host machine
                        echo "--- Running build on the Windows host machine ---"
                        bat """
                            echo 'Preparing Build Environment'
                            node --version
                            npm --version
                            npm ci
                            echo 'Before build'
                            dir
                            npm run build
                            echo 'After build'
                            dir
                        """
                        bat """
                            if not exist "build\\index.html" (
                                echo "index.html does not exist into build folder."
                                exit /b 1
                            )
                            echo "index.html exists into build folder."
                            npm test
                        """
                    }
                }
            }
        }
    }
}