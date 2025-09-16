pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Without Docker - Pipeline'
                bat 'dir /s'
            }
        }
    }
    post {
        always {
            cleanWs()       
        }
    }
}
