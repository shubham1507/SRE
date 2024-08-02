pipeline {
    agent any

    environment {
        ANSIBLE_HOST_KEY_CHECKING = 'False'
        GIT_REPO_URL = 'https://github.com/shubham1507/SRE.git'
        GIT_BRANCH = 'master' // Adjust if needed
    }

    stages {
        stage('Checkout Repository') {
            steps {
                script {
                    // Clone the GitHub repository
                    git branch: "${env.GIT_BRANCH}", url: "${env.GIT_REPO_URL}"
                }
            }
        }
        stage('Install Jenkins on Target Server') {
            steps {
                script {
                    sh """
                        ansible-playbook -i inventory install_jenkins.yml
                    """
                }
            }
        }
        stage('Install Nexus on Nexus Server') {
            steps {
                script {
                    sh """
                        ansible-playbook -i inventory install_nexus.yml
                    """
                }
            }
        }
        stage('Install MicroK8s on Kubernetes Server') {
            steps {
                script {
                    sh """
                        ansible-playbook -i inventory install_microk8s.yml
                    """
                }
            }
        }
    }
}
