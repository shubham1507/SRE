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

        stage('Check Tools') {
            steps {
                script {
                    sh '''
                        #!/bin/bash
                        set -e

                        echo "Checking PATH: $PATH"
                        echo "Checking for unzip..."
                        which unzip || { echo "unzip not found in PATH"; exit 1; }

                        echo "Checking for terraform..."
                        if ! command -v terraform &> /dev/null; then
                            echo "Terraform not found. Installing..."
                            wget https://releases.hashicorp.com/terraform/1.0.0/terraform_1.0.0_linux_amd64.zip
                            unzip terraform_1.0.0_linux_amd64.zip
                            
                            # Move terraform to a directory where Jenkins has write permissions
                            mkdir -p $HOME/bin
                            mv terraform $HOME/bin/
                            echo "Added $HOME/bin to PATH"
                            export PATH=$HOME/bin:$PATH
                            terraform --version
                        else
                            echo "Terraform is installed."
                        fi
                    '''
                }
            }
        }

        stage('Terraform Init') {
            steps {
                sh 'terraform init'
            }
        }

        stage('Terraform Apply') {
            steps {
                sh 'terraform apply -auto-approve'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
