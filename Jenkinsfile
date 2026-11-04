pipeline {
    agent any

    tools { maven 'Maven3' }

    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers { githubPush() }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Maven Clean & Compile') {
            steps { sh 'mvn -B clean compile' }
        }
        stage('JUnit Testing') {
            steps { sh 'mvn -B test' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }
        stage('Package Application') {
            steps { sh 'mvn -B package -DskipTests' }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/BankingMicroservice.jar', fingerprint: true
            }
        }
    }
}