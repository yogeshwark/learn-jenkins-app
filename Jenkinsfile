pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                echo 'Without Docker - Pipeline'
                node --version
                npm --version
                npm ci
                bat 'dir'
                npm run build
                bat 'dir'
            }
        }
    }
}
