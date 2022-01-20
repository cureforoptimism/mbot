pipeline {
   agent any
   environment {
       registry = "labmain:13000/mbot_delete_me"
   }
   stages {
       stage('Build Dockerfile and Publish') {
           steps{
               script {
                   def appimage = docker.build registry + ":$BUILD_NUMBER"
                   docker.withRegistry( '', registryCredential ) {
                       appimage.push()
                       appimage.push('latest')
                   }
               }
           }
       }
//       stage ('Deploy') {
    //        steps {
    //            script{
    //                def image_id = registry + ":$BUILD_NUMBER"
    //                sh "ansible-playbook  playbook.yml --extra-vars \"image_id=${image_id}\""
    //            }
    //        }
    //    }
   }
}