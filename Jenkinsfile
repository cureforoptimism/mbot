pipeline {
   agent { dockerfile true }
   environment {
       registry = "labmain/mbot"
   }
   stages {
       stage ('Deploy') {
           steps {
               script{
                   def image_id = registry + ":$BUILD_NUMBER"
                   sh "echo ${image_id}"
                   // sh "ansible-playbook  playbook.yml --extra-vars \"image_id=${image_id}\""
               }
           }
       }
   }
}
