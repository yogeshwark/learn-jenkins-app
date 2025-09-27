pipeline {
    agent any

    environment {
        NETLIFLY_SITE_ID = '2e0f6ad8-8942-406d-a56f-b34feb065333'
        NETLIFLY_AUTH_TOKEN = credentials('netlify-token')
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
        stage('Deploy') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running deploy on a Linux Docker agent ---"
                        docker.image('node:18-alpine').inside {
                            sh '''
                                echo "--- Checking Netlify CLI version ---"
                                npm install netlify-cli
                                npx netlify --version
                                DEPLOY_OUTPUT=$(npx netlify deploy --dir=build --prod --site=$NETLIFLY_SITE_ID --auth=$NETLIFLY_AUTH_TOKEN)
                                echo "$DEPLOY_OUTPUT"
                                def deployedUrl = (DEPLOY_OUTPUT =~ /URL:\\s*(.*)/)[0][1]
                                env.DEPLOYED_URL = deployedUrl
                                echo "Deployed URL: ${env.DEPLOYED_URL}"
                                echo "--- Netlify CLI version checked ---"
                                echo "--- Deployment to Netlify is successful ---"
                            '''
                        }
                    } else {
                        echo "--- Running deploy on the Windows host machine ---"
                        def deployOutput = bat (script: """
                            echo "--- Checking Netlify CLI version ---"
                            npm install netlify-cli
                            npx netlify --version
                            echo "--- Netlify CLI version checked ---"
                            SET "DEPLOY_OUTPUT_ACCUMULATOR="
                            FOR /F "tokens=*" %%i IN ('npx netlify deploy --dir=build --prod --site=%NETLIFLY_SITE_ID% --auth=%NETLIFLY_AUTH_TOKEN%') DO (
                                ECHO %%i
                                SET "DEPLOY_OUTPUT_ACCUMULATOR=!DEPLOY_OUTPUT_ACCUMULATOR!%%i\n"
                            )
                            echo "--- Deployment to Netlify is successful ---"
                            echo "%DEPLOY_OUTPUT_ACCUMULATOR%"
                        """, returnStdout: true).trim()

                        def deployedUrl = (deployOutput =~ /URL:\\s*(.*)/)[0][1]
                        env.DEPLOYED_URL = deployedUrl
                        echo "Deployed URL: ${env.DEPLOYED_URL}"
                    }
                }
            }
        }
        stage('Post-Deploy Tests') {
            steps {
                script {
                    if (isUnix()) {
                        echo "--- Running Post-Deploy E2E tests on a Linux Docker agent ---"
                        docker.image('mcr.microsoft.com/playwright:v1.39.0-jammy').inside {
                            sh '''
                                npm ci
                                npx playwright test --project=chromium --base-url=${DEPLOYED_URL}
                            '''
                        }
                    } else {
                        echo "--- Running Post-Deploy E2E tests on the Windows host machine ---"
                            bat """
                                npm ci
                                npx playwright test --project=chromium --base-url=%DEPLOYED_URL%
                            """
                    }
                }
            }
            post {
                always {
                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, icon: '', keepAll: false, reportDir: 'playwright-report-post-deploy', reportFiles: 'index.html', reportName: 'Playwright HTML Report (Post-Deploy)', reportTitles: '', useWrapperFileDirectly: true])
                }
            }
        }
    }
}
