pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/shubham1507/SRE.git'
        INVENTORY_FILE = 'inventory.ini' // Adjusted path if file is at root
        PLAYBOOK_FILE = 'install_tools.yml' // Adjusted path if file is at root
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'master', url: "${GIT_REPO}"
                sh 'ls -R' // Recursively list files to confirm directory structure
            }
        }

        stage('Check Server Reachability') {
            steps {
                script {
                    def inventoryFile = readFile("${INVENTORY_FILE}")
                    def servers = inventoryFile.split('\n').findAll { it && !it.startsWith('[') && !it.startsWith(';') && !it.startsWith('#') }
                    def reachable = true
                    servers.each { line ->
                        def server = line.split()[0]
                        def result = sh script: "ping -c 1 ${server}", returnStatus: true
                        if (result != 0) {
                            echo "Server ${server} is not reachable"
                            reachable = false
                        }
                    }
                    if (!reachable) {
                        error "One or more servers are not reachable"
                    }
                }
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ssh-credentials-id', keyFileVariable: 'SSH_KEY')]) {
                    writeFile file: 'ansible.cfg', text: """
                    [defaults]
                    host_key_checking = False
                    """
                    sh """
                        ansible-playbook -i ${INVENTORY_FILE} ${PLAYBOOK_FILE} --private-key=${SSH_KEY}
                    """
                }
            }
        }
    }
}
