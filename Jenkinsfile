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
    }

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
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }
        stage('Run Test Container') {
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
            steps { echo 'Testing is successful' }
        }
        stage('Push to Docker Hub') {
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
        always {
            sh 'docker image prune -f || true'
        }
    }
}