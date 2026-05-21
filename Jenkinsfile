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
                sh 'docker stop freelancelk-frontend freelancelk-backend freelancelk-db || true'
                sh 'docker rm freelancelk-frontend freelancelk-backend freelancelk-db || true'
                sh 'docker volume create postgres_data || true'
                sh 'docker volume create uploads_data || true'
                sh 'docker-compose up -d --build'
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