pipeline {
    agent any

    stages {
        stage('w/o docker') {
            steps {
                sh 'echo "Without docker"'
            }
        }

        stage('Build Stage') {
            agent {
                docker {
                    image 'node:18-alpine'
                    reuseNode true
                }
            }
            steps {
                sh '''
                    echo "------Environment setting Docker container------"
                    node -v
                    npm -v
                    echo "------Building the project------"
                    npm ci
                    npm run build
                    echo "------Build completed------"
                '''
            }
        }

        stage('Unit Tests') {
            agent {
                docker {
                    image 'node:18-alpine'
                    reuseNode true
                }
            }
            steps {
                sh '''
                    echo "------Running Unit Tests------"
                    npm test
                    echo "------Unit Tests completed------"
                '''
            }
            post {
                always {
                    junit 'test-results/junit.xml'
                }
            }
        }

        stage('Playwright E2E Tests') {
            agent {
                docker {
                    image 'mcr.microsoft.com/playwright:v1.39.0-jammy'
                    reuseNode true
                }
            }
            steps {
                sh '''
                    echo "------Running Playwright E2E Tests------"
                    npm ci
                    npm start &
                    APP_PID=$!
                    sleep 10 # Give the application some time to start
                    npx playwright test
                    kill $APP_PID
                    echo "------Playwright E2E Tests completed------"
                '''
            }
            post {
                always {
                    publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'playwright-report',
                        reportFiles: 'index.html',
                        reportName: 'Playwright Report'
                    ])
                }
            }
        }
    }
}
