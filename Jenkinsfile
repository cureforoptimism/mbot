pipeline {
   agent any
   environment {
       registry = "labmain:32000/mbot_delete_me"
   }
   stages {
       stage('Build Dockerfile and Publish') {
           environment {
               TOKENS = credentials('tokens.properties')
           }
           steps{
               script {
                   sh "cp ${TOKENS} ${WORKSPACE}/src/main/resources"
                   def appimage = docker.build registry + ":$BUILD_NUMBER"
                   docker.withRegistry( '', '' ) {
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