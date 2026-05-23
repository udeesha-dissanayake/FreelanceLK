pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'freelancelk'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/udeesha-dissanayake/FreelanceLK.git'
            }
        }

        stage('Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([string(credentialsId: 'VITE_GEMINI_KEY', variable: 'VITE_GEMINI_KEY')]) {
                    sh 'docker-compose down --remove-orphans'
                    sh 'VITE_GEMINI_KEY=$VITE_GEMINI_KEY docker-compose up -d --build'
                }
            }
        }

    }

    post {
        success {
            echo 'All stages passed — FreelanceLK deployed successfully!'
        }
        failure {
            echo 'Pipeline failed. Deployment was NOT updated. Check logs above.'
        }
    }
}