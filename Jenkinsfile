pipeline {
    agent any

    environment {
        NETLIFY_SITE_ID = '2e0f6ad8-8942-406d-a56f-b34feb065333'
        NETLIFY_AUTH_TOKEN = credentials('netlify-token')
    }

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
        stage('Test and E2E Test') {
            parallel {
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
                    post {
                        always {
                            junit 'jest-results/junit.xml'
                        }
                    }
                }
                stage('E2E Test') {
                    steps {
                        script {
                            if (isUnix()) {
                                echo "--- Running E2E tests on a Linux Docker agent ---"
                                docker.image('mcr.microsoft.com/playwright:v1.39.0-jammy').inside {
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

                    post {
                        always {
                            publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, icon: '', keepAll: false, reportDir: 'playwright-report', reportFiles: 'index.html', reportName: 'Playwright HTML Report', reportTitles: '', useWrapperFileDirectly: true])
                        }
                    }
                }
            }
        }
        stage('Deploy Stage Environment') {
            steps {
                script {
                    def deployOutput
                    if (isUnix()) {
                        echo "--- Running stage deploy on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh 'npm install netlify-cli'
                            deployOutput = sh(script: "node_modules/.bin/netlify deploy --dir=build --no-build --site=$NETLIFY_SITE_ID --auth=$NETLIFY_AUTH_TOKEN", returnStdout: true)
                        }
                    } else {
                        echo "--- Running stage deploy on the Windows host machine ---"
                        bat 'npm install netlify-cli'
                        deployOutput = bat(script: "node_modules/.bin/netlify deploy --dir=build --site %NETLIFY_SITE_ID% --auth %NETLIFY_AUTH_TOKEN%", returnStdout: true)
                    }
                    def deployUrl = deployOutput.split('\n').find { it.contains('Deploy URL:') }?.split(' ')[2]?.trim()
                    env.CI_ENVIRONMENT_URL_STAGE = deployUrl
                    echo "Stage Deploy URL: ${env.CI_ENVIRONMENT_URL_STAGE}"
                }
            }
        }

        stage('E2E Test Stage Environment') {
            environment {
                CI_ENVIRONMENT_URL = env.CI_ENVIRONMENT_URL_STAGE
            }
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running E2E tests on a Linux Docker agent for Stage ---"
                        docker.image('mcr.microsoft.com/playwright:v1.39.0-jammy').inside {
                            sh '''
                                npm ci
                                npx playwright test
                            '''
                        }
                    } else {
                        echo "--- Running E2E tests on the Windows host machine for Stage ---"
                            bat """
                                npm ci
                                npx playwright test
                            """
                    }
                }
            }
            post {
                always {
                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, icon: '', keepAll: false, reportDir: 'playwright-report', reportFiles: 'index.html', reportName: 'Playwright HTML Report (Stage Deploy)', reportTitles: '', useWrapperFileDirectly: true])
                }
            }
        }

        stage('Approval') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input message: 'Approve to deploy to Production?', ok: 'Deploy'
                }
            }
        }
        
        stage('Deploy Production') {
            steps {
                script {
                    def deployOutput
                    if (isUnix()) {
                        echo "--- Running deploy on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh 'npm install netlify-cli'
                            deployOutput = sh(script: "node_modules/.bin/netlify deploy --prod --dir=build --no-build --site=$NETLIFY_SITE_ID --auth=$NETLIFY_AUTH_TOKEN", returnStdout: true)
                        }
                    } else {
                        echo "--- Running deploy on the Windows host machine ---"
                        bat 'npm install netlify-cli'
                        deployOutput = bat(script: "node_modules/.bin/netlify deploy --dir=build --prod --site %NETLIFY_SITE_ID% --auth %NETLIFY_AUTH_TOKEN%", returnStdout: true)
                    }
                    def deployUrl = deployOutput.split('\n').find { it.contains('Deploy URL:') }?.split(' ')[2]?.trim()
                    env.CI_ENVIRONMENT_URL_PROD = deployUrl
                    echo "Production Deploy URL: ${env.CI_ENVIRONMENT_URL_PROD}"
                }
            }
        }
        stage('Post-Deploy Tests Production') {
            environment {
                CI_ENVIRONMENT_URL = env.CI_ENVIRONMENT_URL_PROD
            }
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running Post-Deploy E2E tests on a Linux Docker agent ---"
                        docker.image('mcr.microsoft.com/playwright:v1.39.0-jammy').inside {
                            sh '''
                                npm ci
                                npx playwright test
                            '''
                        }
                    } else {
                        echo "--- Running Post-Deploy E2E tests on the Windows host machine ---"
                            bat """
                                npm ci
                                npx playwright test
                            """
                    }
                }
            }
            post {
                always {
                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, icon: '', keepAll: false, reportDir: 'playwright-report', reportFiles: 'index.html', reportName: 'Playwright HTML Report (Post-Deploy)', reportTitles: '', useWrapperFileDirectly: true])
                }
            }
        }
    }
}
