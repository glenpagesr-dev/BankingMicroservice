pipeline {
    agent any

    tools { maven 'Maven3' }

    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers { githubPush() }

    environment {
        IMAGE_NAME     = 'glentp/bankingmicroservice'
        IMAGE_TAG      = "v${BUILD_NUMBER}"
        TEST_CONTAINER = 'banking-test'
        DEV_EMAILS     = 'glenpage.sr@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Maven Clean & Compile') {
            steps { sh 'mvn -B clean compile' }
        }
        stage('JUnit Testing') {
            steps { sh 'mvn -B test -Dmaven.test.failure.ignore=true' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }
        stage('Package Application') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps { sh 'mvn -B package -DskipTests' }
        }
        stage('Archive Artifacts') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                archiveArtifacts artifacts: 'target/BankingMicroservice.jar', fingerprint: true
            }
        }
        stage('Build Docker Image') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }
        stage('Run Test Container') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                sh '''
                    docker rm -f $TEST_CONTAINER || true
                    docker run -d --name $TEST_CONTAINER -p 8081:8080 $IMAGE_NAME:$IMAGE_TAG
                    for i in $(seq 1 15); do
                        if curl -fsS http://localhost:8081/health; then
                            echo ""
                            echo "Health check passed"
                            exit 0
                        fi
                        sleep 2
                    done
                    echo "Health check failed"
                    docker logs $TEST_CONTAINER
                    exit 1
                '''
            }
        }
        stage('Testing') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps { echo 'Testing is successful' }
        }
        stage('Push to Docker Hub') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                        echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                        docker push $IMAGE_NAME:latest
                        docker logout
                    '''
                }
            }
        }
    }

    post {
        unsuccessful {
            emailext(
                to: "${DEV_EMAILS}",
                subject: "${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """The Jenkins pipeline did not complete successfully.

Project Name: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build Status: ${currentBuild.currentResult}
Jenkins URL:  ${env.JENKINS_URL}
Console Output: ${env.BUILD_URL}console

Deployment was stopped. Please review the console output."""
            )
        }
        fixed {
            emailext(
                to: "${DEV_EMAILS}",
                subject: "FIXED: ${env.JOB_NAME} #${env.BUILD_NUMBER} is back to normal",
                body: """The pipeline is passing again.

Project Name: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build Status: ${currentBuild.currentResult}
Build URL:    ${env.BUILD_URL}"""
            )
        }
        always {
            sh 'docker image prune -f || true'
        }
    }
}