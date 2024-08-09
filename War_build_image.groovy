pipeline {
    agent any

    environment {
        NEXUS_URL = 'http://192.168.10.36:8081/repository/maven-releases/'
        NEXUS_CREDENTIALS_ID = 'nexus'
        DOCKER_CREDENTIALS_ID = 'dockerhub'
        DOCKER_IMAGE_NAME = '839024/war_build_image'
        DOCKER_IMAGE_TAG = 'latest'
        WAR_FILE_NAME = 'myweb-0.0.5.war'
        GROUP_ID = 'com.example'
        ARTIFACT_ID = 'mywebapp'
        VERSION = '0.0.5'
    }

    tools {
        maven 'mvn_dryrun'
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'master', url: 'https://github.com/shubham1507/mywebapp.git'
            }
        }

        stage('Build WAR') {
            steps {
                script {
                    sh 'mvn clean package'
                }
            }
        }

        stage('Verify WAR File Presence') {
            steps {
                script {
                    sh 'ls -al target/' // Check if WAR file is present in the target directory
                }
            }
        }

        stage('Deploy WAR to Nexus') {
            steps {
                script {
                    def warFilePath = "target/${WAR_FILE_NAME}"
                    if (fileExists(warFilePath)) {
                        echo "Deploying WAR file: ${warFilePath}"
                        withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS_ID}", usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                            sh """
                            curl -v -u ${NEXUS_USERNAME}:${NEXUS_PASSWORD} --upload-file ${warFilePath} \
                                ${NEXUS_URL}${GROUP_ID.replace('.', '/')}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}.war
                            """
                        }
                    } else {
                        error "WAR file not found: ${warFilePath}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerfileContent = """
                    FROM openjdk:11-jre
                    RUN apt-get update && apt-get install -y curl
                    ENV NEXUS_URL=${NEXUS_URL}
                    RUN curl -O ${NEXUS_URL}${GROUP_ID.replace('.', '/')}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}.war
                    COPY target/${WAR_FILE_NAME} /usr/local/tomcat/webapps/${WAR_FILE_NAME}
                    CMD ["catalina.sh", "run"]
                    """

                    writeFile file: 'Dockerfile', text: dockerfileContent

                    // Build Docker image
                    sh 'docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} -f Dockerfile .'
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}").push("${DOCKER_IMAGE_TAG}")
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
