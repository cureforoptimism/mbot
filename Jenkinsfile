pipeline {
   agent any
   environment {
       registry = "labmain:13000/mbot_delete_me"
   }
   stages {
       stage('Build Deps Image') {
           agent {
               docker {
                   image 'amazoncorretto:17'
               }
           }
           steps {
               // Create our project directory.
               sh 'mkdir -p /mbot'
               sh 'cd /mbot'
               sh 'cp ${WORKSPACE}/build.gradle /mbot && cp ${WORKSPACE}/settings.gradle /mbot && cp ${WORKSPACE}/gradlew /mbot'

               // Build dependency layer
               sh './gradlew bootJar 2>/dev/null || true'

               // Copy all files in our Jenkins workspace to our project directory.
               sh 'cp -r ${WORKSPACE}/* /mbot'
               // Build the app.
               sh './gradlew bootJar'
           }
       }
       stage('Publish') {
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