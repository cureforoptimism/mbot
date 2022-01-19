pipeline {
   agent { dockerfile true }
   environment {
       registry = "labmain/mbot"
   }
   stages {
       stage('Publish') {
           steps{
               script {
                   sh "echo $PATH
                   def appimage = docker.build registry + ":$BUILD_NUMBER"
                   docker.withRegistry( '', '' ) {
                       appimage.push()
                       appimage.push('latest')
                   }
               }
           }
       }
       stage ('Deploy') {
           steps {
               script{
                   def image_id = registry + ":$BUILD_NUMBER"
                   sh "ansible-playbook  playbook.yml --extra-vars \"image_id=${image_id}\""
               }
           }
       }
   }
}
