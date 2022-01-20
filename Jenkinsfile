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
               sh 'mkdir -p ${WORKSPACE}/temp'
               sh 'cd ${WORKSPACE}/temp'
               sh 'cp ${WORKSPACE}/build.gradle ${WORKSPACE}/temp && cp ${WORKSPACE}/settings.gradle ${WORKSPACE}/temp && cp ${WORKSPACE}/gradlew ${WORKSPACE}/temp'

               // Build dependency layer
               sh './gradlew bootJar 2>/dev/null || true'
               sh 'rm -Rf ${WORKSPACE}/temp'

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