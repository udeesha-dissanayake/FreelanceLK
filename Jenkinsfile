pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/udeesha-dissanayake/FreelanceLK.git'
            }
        }

        stage('Build and Deploy') {
            steps {
                echo 'Building and deploying...'
                withCredentials([string(credentialsId: 'VITE_GEMINI_KEY', variable: 'VITE_GEMINI_KEY')]) {
                    sh '''
                        docker stop freelancelk-frontend freelancelk-backend freelancelk-db || true
                        docker rm freelancelk-frontend freelancelk-backend freelancelk-db || true
                        docker volume create postgres_data || true
                        docker volume create uploads_data || true
                        VITE_GEMINI_KEY=$VITE_GEMINI_KEY docker-compose up -d --build
                    '''
                }
            }
        }

    }

    post {
        success {
            echo 'Deployed successfully!'
        }
        failure {
            echo 'Build failed. Check the logs above.'
        }
    }
}