pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running build on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh '''
                                echo "--- Running in the environment ---"
                                node --version
                                npm --version
                                npm ci
                                npm run build
                                echo "--- Build is successful ---"
                            '''
                        }
                    } else {
                        echo "--- Running build on the Windows host machine ---"
                        bat """
                            echo "--- Running in the environment ---"
                            node --version
                            npm --version
                            npm ci
                            npm run build
                             echo "--- Build is successful ---"
                        """
                    }
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running unit tests on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh 'npm test'
                        }
                    } else {
                        echo "--- Running unit tests on the Windows host machine ---"
                        bat 'npm test'
                    }
                }
            }
        }
        stage('E2E Test') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running E2E tests on a Linux Docker agent ---"
                        docker.image('mcr.microsoft.com/playwright:v1.50.0-noble').inside {
                            sh '''
                                npm install serve
                                npx serve -s build -l 3000 &
                                # SERVER_PID=$!
                                sleep 10
                                npx playwright test
                                # kill $SERVER_PID
                            '''
                        }
                    } else {
                        echo "--- Running E2E tests on the Windows host machine ---"
                        bat """
                            npm install serve
                            start /B npx serve -s build -l 3000
                            sleep 10
                            npx playwright test
                            REM Assuming Jenkins or the test runner will clean up the background server process.
                            REM If not, a more explicit kill command would be needed, e.g., taskkill /F /IM node.exe /FI "LISTENERS eq 3000"
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            junit 'test-results/junit.xml'
        }
    }
}
