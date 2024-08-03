pipeline {
    agent any

    environment {
        ANSIBLE_HOST_KEY_CHECKING = 'False'
        GIT_REPO_URL = 'https://github.com/shubham1507/SRE.git'
        GIT_BRANCH = 'master' // Adjust if needed
        SSH_PRIVATE_KEY = credentials('ssh-private-key-id') // Add your SSH private key credential ID here
    }

    stages {
        stage('Checkout Repository') {
            steps {
                script {
                    git branch: "${env.GIT_BRANCH}", url: "${env.GIT_REPO_URL}"
                }
            }
        }

        stage('Check SSH Connectivity') {
            steps {
                script {
                    // Set up SSH configuration
                    writeFile file: '/tmp/ssh_config', text: '''
                        Host target
                            Hostname 10.0.1.133
                            User user
                            IdentityFile /tmp/private_key
                    '''
                    // Add private key to a temporary file
                    writeFile file: '/tmp/private_key', text: "${env.SSH_PRIVATE_KEY}"
                    sh '''
                        chmod 600 /tmp/private_key
                        chmod 600 /tmp/ssh_config
                        ssh -F /tmp/ssh_config -o StrictHostKeyChecking=no target "echo 'SSH connection successful'"
                    '''
                }
            }
        }

        stage('Install Jenkins on Target Server') {
            steps {
                script {
                    sh '''
                        ansible-playbook -i inventory install_jenkins.yml
                    '''
                }
            }
        }

        stage('Install Nexus on Nexus Server') {
            steps {
                script {
                    sh '''
                        ansible-playbook -i inventory install_nexus.yml
                    '''
                }
            }
        }

        stage('Install MicroK8s on Kubernetes Server') {
            steps {
                script {
                    sh '''
                        ansible-playbook -i inventory install_microk8s.yml
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed. Please check the logs for more details.'
        }
    }
}
