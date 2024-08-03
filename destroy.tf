pipeline {
    agent any

    environment {
        AWS_DEFAULT_REGION = "ap-south-1"
    }

    stages {
        stage('Checkout') {
            steps {
           
                git "https://github.com/shubham1507/SRE.git"
            }
        }

        stage('Terraform Init') {
            steps {
                script {
                    // Initialize Terraform
                    sh 'terraform init'
                }
            }
        }

        stage('Terraform Plan') {
            steps {
                script {
                    // Generate a Terraform execution plan
                    sh 'terraform plan -out=tfplan'
                }
            }
        }

        stage('Terraform Destroy') {
            steps {
                script {
                    // Apply the Terraform destroy command
                    sh 'terraform destroy -auto-approve'
                }
            }
        }
    }

    post {
        always {
            // Clean up the workspace
            cleanWs()
        }
    }
}
