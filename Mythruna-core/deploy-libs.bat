cd ..
md Mythruna-Server-20120627-Windows
cd Mythruna-Server-20120627-Windows
jar xf ../Mythruna-Server-20120627-Windows.zip
cd ..
cd Mythruna-core

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/groovy-all-1.8.0.jar -DgroupId=com.mythruna.server -DartifactId=groovy-all -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/hsqldb.jar -DgroupId=com.mythruna.server -DartifactId=hsqldb -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/jME3-core.jar -DgroupId=com.mythruna.server -DartifactId=jME3-core -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/jME3-networking.jar -DgroupId=com.mythruna.server -DartifactId=jME3-networking -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/log4j-1.2.12.jar -DgroupId=com.mythruna.server -DartifactId=log4j -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/meta-jb-cmd-1.0.0.jar -DgroupId=com.mythruna.server -DartifactId=meta-jb-cmd -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/meta-jb-core-0.25.1-SNAPSHOT.jar -DgroupId=com.mythruna.server -DartifactId=meta-jb-core -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/meta-jb-json-1.0.1.jar -DgroupId=com.mythruna.server -DartifactId=meta-jb-json -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/meta-jb-swing-0.25.1-SNAPSHOT.jar -DgroupId=com.mythruna.server -DartifactId=meta-jb-swing -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-Server-20120627-Windows/repo -Dfile=../Mythruna-Server-20120627-Windows/lib/meta-jb-util-0.25.1-SNAPSHOT.jar -DgroupId=com.mythruna.server -DartifactId=meta-jb-util -Dpackaging=jar -Dversion=1.0