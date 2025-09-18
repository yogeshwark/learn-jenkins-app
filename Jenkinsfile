pipeline {
    agent any 
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
                    echo 'Preparing Build Environment'
                    npm ci
                    echo 'Before build'
                    dir
                    npm run build
                    echo 'After build'
                    dir
                '''
            }
        }
        stage('Test on Windows') {
            when {
                // Ensure this stage only runs on Linux agents
                expression {
                    return isUnix() == true
                }
            }
            steps {
                powershell '''
                   if (Test-Path -Path "index.html") {
                        Write-Host "index.html exists."
                    } else {
                        Write-Host "index.html does not exist."
                        exit 1
                    }
                '''  
            }
        }
    }
}
