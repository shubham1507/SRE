pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        TF_VAR_aws_access_key = credentials('aws-access-key-id')
        TF_VAR_aws_secret_key = credentials('aws-secret-access-key')
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/shubham1507/SRE.git'
            }
        }

        stage('Setup Terraform') {
            steps {
                script {
                    sh '''
                        #!/bin/bash
                        set -e

                        echo "Checking PATH: $PATH"

                        # Install unzip if not available
                        if ! command -v unzip &> /dev/null; then
                            echo "unzip not found. Installing..."
                            sudo apt-get update && sudo apt-get install -y unzip
                        else
                            echo "unzip is installed."
                        fi

                        echo "Checking for terraform..."
                        if ! command -v terraform &> /dev/null; then
                            echo "Terraform not found. Installing..."

                            # Define version and download URL
                            VERSION="1.0.0"
                            URL="https://releases.hashicorp.com/terraform/${VERSION}/terraform_${VERSION}_linux_amd64.zip"
                            
                            # Download Terraform
                            wget $URL -O terraform.zip

                            # Unzip the downloaded file
                            unzip terraform.zip

                            # Move terraform to a writable directory within the workspace
                            mkdir -p $WORKSPACE/bin
                            mv terraform $WORKSPACE/bin/
                            echo "Added $WORKSPACE/bin to PATH"

                            # Ensure PATH is updated in this script's environment
                            export PATH=$WORKSPACE/bin:$PATH
                            echo "PATH: $PATH"

                            # Verify installation
                            terraform --version
                        else
                            echo "Terraform is already installed."
                            terraform --version
                        fi
                    '''
                }
            }
        }

        stage('Terraform Init') {
            steps {
                script {
                    // Add $WORKSPACE/bin to PATH for this stage
                    withEnv(["PATH+WORKSPACE=${env.WORKSPACE}/bin"]) {
                        sh 'terraform init'
                    }
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                script {
                    // Add $WORKSPACE/bin to PATH for this stage
                    withEnv(["PATH+WORKSPACE=${env.WORKSPACE}/bin"]) {
                        sh 'terraform apply -auto-approve'
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
