pipeline {
   agent any
   environment {
       registry = "registry.homelab.com/mbot"
   }
   stages {
       stage('Build Dockerfile and Publish') {
           environment {
               TOKENS = credentials('tokens.properties')
           }
           steps{
               script {
                   sh "rm -f ${WORKSPACE}/src/main/resources/tokens.properties"
                   sh "cp ${TOKENS} ${WORKSPACE}/src/main/resources"
                   def appimage = docker.build registry + ":$BUILD_NUMBER"
                   docker.withRegistry( 'https://registry.homelab.com', 'docker-creds' ) {
                       appimage.push()
                       appimage.push('latest')
                   }
                   sh "rm -f ${WORKSPACE}/src/main/resources/tokens.properties"
               }
           }
       }
      stage ('Deploy') {
           steps {
               script{
                   def image_id = "registry.homelab.com/mbot" + ":$BUILD_NUMBER"
                   sh "ansible-playbook  playbook.yml --extra-vars \"image=${image_id}\""
               }
           }
       }
   }
}
