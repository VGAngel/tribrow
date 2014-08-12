cd ..
md Mythruna-20120627-Windows
cd Mythruna-20120627-Windows
jar xf ../Mythruna-20120627-Windows.zip
cd ..
cd Mythruna-client

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/assets.jar -DgroupId=com.mythruna.client -DartifactId=assets -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/eventbus.jar -DgroupId=com.mythruna.client -DartifactId=eventbus -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/groovy-all-1.8.0.jar -DgroupId=com.mythruna.client -DartifactId=groovy-all -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/hsqldb.jar -DgroupId=com.mythruna.client -DartifactId=hsqldb -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/j-ogg-oggd.jar -DgroupId=com.mythruna.client -DartifactId=j-ogg-oggd -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/j-ogg-vorbisd.jar -DgroupId=com.mythruna.client -DartifactId=j-ogg-vorbisd -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jinput.jar -DgroupId=com.mythruna.client -DartifactId=jinput -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-core.jar -DgroupId=com.mythruna.client -DartifactId=jME3-core -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-desktop.jar -DgroupId=com.mythruna.client -DartifactId=jME3-desktop -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-effects.jar -DgroupId=com.mythruna.client -DartifactId=jME3-effects -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-jogg.jar -DgroupId=com.mythruna.client -DartifactId=jME3-jogg -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-lwjgl.jar -DgroupId=com.mythruna.client -DartifactId=jME3-lwjgl -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-lwjgl-natives.jar -DgroupId=com.mythruna.client -DartifactId=jME3-lwjgl-natives -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-networking.jar -DgroupId=com.mythruna.client -DartifactId=jME3-networking -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-niftygui.jar -DgroupId=com.mythruna.client -DartifactId=jME3-niftygui -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/jME3-plugins.jar -DgroupId=com.mythruna.client -DartifactId=jME3-plugins -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/log4j-1.2.12.jar -DgroupId=com.mythruna.client -DartifactId=log4j -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/lwjgl.jar -DgroupId=com.mythruna.client -DartifactId=lwjgl -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/meta-jb-cmd-1.0.0.jar -DgroupId=com.mythruna.client -DartifactId=meta-jb-cmd -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/meta-jb-json-1.0.1.jar -DgroupId=com.mythruna.client -DartifactId=meta-jb-json -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/meta-jb-shell-1.1.1-SNAPSHOT.jar -DgroupId=com.mythruna.client -DartifactId=meta-jb-shell -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/meta-jb-util-0.25.1-SNAPSHOT.jar -DgroupId=com.mythruna.client -DartifactId=meta-jb-util -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/Mythruna-core.jar -DgroupId=com.mythruna.client -DartifactId=Mythruna-core -Dpackaging=jar -Dversion=1.0

call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/nifty.jar -DgroupId=com.mythruna.client -DartifactId=nifty -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/nifty-default-controls.jar -DgroupId=com.mythruna.client -DartifactId=nifty-default-controls -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/nifty-style-black.jar -DgroupId=com.mythruna.client -DartifactId=nifty-style-black -Dpackaging=jar -Dversion=1.0
call mvn deploy:deploy-file -Durl=file:///%CD%/../Mythruna-20120627-Windows/repo -Dfile=../Mythruna-20120627-Windows/lib/xmlpull-xpp3.jar -DgroupId=com.mythruna.client -DartifactId=xmlpull-xpp3 -Dpackaging=jar -Dversion=1.0