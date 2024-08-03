pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/shubham1507/SRE.git'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
        INVENTORY_FILE = 'inventory.ini'
        SSH_KEY_PATH = '/var/lib/jenkins/.ssh/id_rsa.pem'
        USER = 'ubuntu'
    }

    stages {
        stage('Generate SSH Key Pair') {
            steps {
                script {
                    sh """
                    if [ ! -f ${env.SSH_KEY_PATH} ]; then
                        echo "Please upload your PEM file to ${env.SSH_KEY_PATH}"
                    fi
                    """
                }
            }
        }

        stage('Checkout Ansible Playbooks') {
            steps {
                git url: "${env.GIT_REPO}"
            }
        }

        stage('Install Tools') {
            steps {
                script {
                    sh """
                    ansible-playbook -i ${env.INVENTORY_FILE} ${env.ANSIBLE_PLAYBOOK} --private-key=${env.SSH_KEY_PATH}
                    """
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/*.log', allowEmptyArchive: true
            cleanWs()
        }
    }
}
