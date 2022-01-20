pipeline {
   agent any
   environment {
       registry = "labmain:32000/mbot"
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
                   docker.withRegistry( '', '' ) {
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
                   def image_id = "localhost" + ":$BUILD_NUMBER"
                   sh "ansible-playbook  playbook.yml --extra-vars \"image=${image_id}\""
               }
           }
       }
   }
}
