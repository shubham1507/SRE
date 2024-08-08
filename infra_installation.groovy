pipeline {
    agent any

    environment {
        // Absolute path to the directory containing the inventory file and playbooks
        ANSIBLE_PLAYBOOKS_PATH = '/home/ubuntu/ansible_playbooks'
        INVENTORY_FILE = "${ANSIBLE_PLAYBOOKS_PATH}/inventory.ini"
    }

    stages {
        stage('Check File Existence') {
            steps {
                script {
                    // Check if the inventory file exists
                    def fileExists = fileExists("${INVENTORY_FILE}")
                    if (!fileExists) {
                        error "The file ${INVENTORY_FILE} does not exist in the workspace."
                    } else {
                        echo "File ${INVENTORY_FILE} found in workspace."
                    }
                }
            }
        }

        stage('Read Inventory and Deploy Tools') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ssh-credentials-id', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    script {
                        // Read the inventory file
                        def inventoryContent = sh(script: "cat ${INVENTORY_FILE}", returnStdout: true).trim()
                        def inventoryLines = inventoryContent.split('\n')
                        
                        def servers = [:]
                        def currentServerType = null

                        // Parse inventory file to extract IP addresses
                        inventoryLines.each { line ->
                            line = line.trim()
                            if (line.startsWith('[')) {
                                // Identify server group
                                if (line.contains('jenkins')) {
                                    currentServerType = 'jenkins'
                                } else if (line.contains('nexus')) {
                                    currentServerType = 'nexus'
                                } else if (line.contains('kubernetes')) {
                                    currentServerType = 'kubernetes'
                                }
                            } else if (line && !line.startsWith('#') && currentServerType) {
                                def parts = line.split()
                                if (parts.size() > 0) {
                                    def ip = parts[0]
                                    servers[currentServerType] = ip
                                }
                            }
                        }

                        // Deploy tools based on extracted IPs
                        if (servers.jenkins) {
                            echo "Installing tools on Jenkins server: ${servers.jenkins}"
                            sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${SSH_USER}@${servers.jenkins} '
                            cd ${ANSIBLE_PLAYBOOKS_PATH} &&
                            ansible-playbook -i ${INVENTORY_FILE} install_tools.yml -e "install_jenkins=True"'
                            """
                        }

                        if (servers.nexus) {
                            echo "Installing tools on Nexus server: ${servers.nexus}"
                            sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${SSH_USER}@${servers.nexus} '
                            cd ${ANSIBLE_PLAYBOOKS_PATH} &&
                            ansible-playbook -i ${INVENTORY_FILE} install_tools.yml -e "install_nexus=True"'
                            """
                        }

                        if (servers.kubernetes) {
                            echo "Installing tools on Kubernetes server: ${servers.kubernetes}"
                            sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${SSH_USER}@${servers.kubernetes} '
                            cd ${ANSIBLE_PLAYBOOKS_PATH} &&
                            ansible-playbook -i ${INVENTORY_FILE} install_tools.yml -e "install_microk8s=True"'
                            """
                        }

                        if (!servers) {
                            error "No valid servers found in the inventory file"
                        }
                    }
                }
            }
        }

        stage('Verify Installation') {
            steps {
                script {
                    echo "Verifying installations..."
                    // Add any verification steps here, if necessary
                }
            }
        }
    }
}
