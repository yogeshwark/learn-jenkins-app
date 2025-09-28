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
                }
            }
            steps {
                sh '''ß
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
    }
}
