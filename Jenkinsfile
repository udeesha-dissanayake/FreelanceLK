pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/udeesha-dissanayake/FreelanceLK.git'
            }
        }

        stage('Build & Deploy') {
            steps {
                sh 'docker-compose down --remove-orphans'
                sh 'docker-compose build --no-cache'
                sh 'docker-compose up -d'
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
