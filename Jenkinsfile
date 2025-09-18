pipeline {
    agent none 
    stages {
        stage('Build on Windows') {
            when {
                // Ensure this stage only runs on Windows agents
                expression {
                    return isUnix() == false
                }
            }
            steps {
                powershell '''
                    $osInfo = Get-CimInstance -ClassName Win32_OperatingSystem
                    Write-Host "OS Version: $($osInfo.Version)"
                    Write-Host "OS Name: $($osInfo.Caption)"

                    echo 'Build without Docker - Pipeline'
                    node --version
                    npm --version
                    npm ci
                    bat 'dir'
                    npm run build
                    bat 'dir'
                '''
            }
        }
    }
}
