pipeline {
    agent any
    
    environment {
        ANSIBLE_GIT_REPO = 'https://github.com/shubham1507/SRE.git'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
        INVENTORY_FILE = 'inventory.ini'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: "${ANSIBLE_GIT_REPO}"
            }
        }
        
        stage('Install Ansible') {
            steps {
                script {
                    sh '''
                    if ! command -v ansible >/dev/null; then
                        echo "Ansible not found. Installing..."
                        sudo apt-get update
                        sudo apt-get install -y ansible
                    else
                        echo "Ansible is already installed."
                    fi
                    '''
                }
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ubuntu', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    script {
                        sh "ansible-playbook -i ${INVENTORY_FILE} ${ANSIBLE_PLAYBOOK} -u ${SSH_USER} --private-key ${SSH_KEY}"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo 'Playbook executed successfully.'
        }
        failure {
            echo 'Playbook execution failed.'
        }
    }
}
