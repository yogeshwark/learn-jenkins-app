pipeline {
    agent any
    environment {
        REACT_APP_VERSION = "1.0.$BUILD_ID"
    }
    stages {
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
                    junit 'jest-results/junit.xml'
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

        stage('Deployment Staging') {
            agent {
                docker {
                    image 'mcr.microsoft.com/playwright:v1.39.0-jammy'
                    reuseNode true
                }
            }
            environment {
                NETLIFY_SITE_ID = credentials('NETLIFY_SITE_ID')
                NETLIFY_AUTH_TOKEN = credentials('netlify-token')
            }
            steps {
                sh '''
                    echo "------Installing Netlify CLI------"
                    npm install netlify-cli
                    echo "------Netlify CLI installed------"
                    echo "------Checking Netlify CLI version------"
                    npx netlify --version
                    echo "------Netlify CLI version checked------"
                    echo "------Deploying to Netlify Staging------"
                '''
                script {
                    def netlifyOutput = sh(script: 'npx netlify deploy --dir=build --no-build --json', returnStdout: true) // Deploy to staging without --prod
                    echo "------Deployment Staging completed------"
                    def deployJson = new groovy.json.JsonSlurperClassic().parseText(netlifyOutput)
                    def deployUrl = deployJson.deploy_url
                    if (deployUrl) {
                        env.CI_ENVIRONMENT_STAGING_URL = deployUrl
                        env.CI_ENVIRONMENT_URL = deployUrl // Set CI_ENVIRONMENT_URL for Playwright tests
                        echo "CI_ENVIRONMENT_STAGING_URL set to: ${env.CI_ENVIRONMENT_STAGING_URL}"
                    } else {
                        error "Could not find deploy_url in Netlify deploy output for staging."
                    }
                }
                sh '''
                    echo "------Running Post-Deployment Playwright E2E Tests (Staging)------"
                    echo "CI_ENVIRONMENT_URL: ${CI_ENVIRONMENT_URL}"
                    npm ci
                    npx playwright test
                    echo "------Post-Deployment Playwright E2E Tests (Staging) completed------"
                '''
            }
            post {
                always {
                    publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'playwright-report-staging',
                        reportFiles: 'index.html',
                        reportName: 'Post-Deployment Playwright Report (Staging)'
                    ])
                }
            }
        }

        stage('Approval Stage') {
            steps {
                script {
                    timeout(time: 1, unit: 'HOURS') {
                        input message: 'Approve deployment to production?'
                    }
                }
            }
        }

        stage('Deployment Stage') {
            agent {
                docker {
                    image 'mcr.microsoft.com/playwright:v1.39.0-jammy'
                    reuseNode true
                }
            }
            environment {
                NETLIFY_SITE_ID = credentials('NETLIFY_SITE_ID')
                NETLIFY_AUTH_TOKEN = credentials('netlify-token')
            }
            steps {
                sh '''
                    echo "------Installing Netlify CLI------"
                    npm install netlify-cli
                    echo "------Netlify CLI installed------"
                    echo "------Checking Netlify CLI version------"
                    npx netlify --version
                    echo "------Netlify CLI version checked------"
                    echo "------Deploying to Netlify------"
                '''
                script {
                    def netlifyOutput = sh(script: 'npx netlify deploy --prod --dir=build --no-build --json', returnStdout: true)
                    echo "------Deployment completed------"
                    def deployJson = new groovy.json.JsonSlurperClassic().parseText(netlifyOutput)
                    def deployUrl = deployJson.deploy_url
                    if (deployUrl) {
                        env.CI_ENVIRONMENT_URL = deployUrl
                        echo "CI_ENVIRONMENT_URL set to: ${env.CI_ENVIRONMENT_URL}"
                    } else {
                        error "Could not find deploy_url in Netlify deploy output."
                    }
                }
                sh '''
                    echo "------Running Post-Deployment Playwright E2E Tests------"
                    echo "CI_ENVIRONMENT_URL: ${CI_ENVIRONMENT_URL}"
                    npm ci
                    npx playwright test
                    echo "------Post-Deployment Playwright E2E Tests completed------"
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
                        reportName: 'Post-Deployment Playwright Report'
                    ])
                }
            }
        }
    }
}
