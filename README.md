# Jacobitus Total (versión de escritorio)
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Maven 3.x.x o Gradle 7.6.2
- ApiDoc

### Instalación de Java 15

Descargamos el JDK desde el sitio web https://jdk.java.net/archive/
```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install wget

$ sudo wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_linux-x64_bin.tar.gz
```

Descomprimimos el archivo **openjdk-15.0.2_linux-x64_bin.tar** en la carpeta **/usr/lib/jvm**.
```bash
$ sudo tar -zxf ./openjdk-15.0.2_linux-x64_bin.tar -C /usr/lib/jvm
```

Registramos la versión de **java**.
```bash
$ sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-15.0.2/bin/java 1502
```

Registramos la versión de **java**.
```bash
$ sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-15.0.2/bin/javac 1502
```

Establecemos Java 15 como versión por defecto.
```bash
$ sudo update-alternatives --config java
```

Seleccionamos la versión que se encuentra en la ruta **/usr/lib/jvm/jdk-15.0.2/bin/java**.

### Instalación maven

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install maven
```

Verificamos la instalación de NVM.
```bash
$ mvn --version
```

### Instalación gradle

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo wget https://services.gradle.org/distributions/gradle-7.6.2-bin.zip
```

```bash
$ mkdir /opt/gradle
$ unzip -d /opt/gradle gradle-7.6.2-bin.zip
$ ls /opt/gradle/gradle-7.6.2
```

Verificamos la instalación de Gradle.
```bash
$ gradle --version
```

### Instalación git

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install git
```

Verificamos la instalación.
```bash
$ git --version
```

## Compilación del proyecto

Clonamos el proyecto desde el Repositorio Estatal de Software Libre.
```
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus4.git
```

### Maven
Ingresamos a la carpeta **jacobitus4** y ejecutar los siguientes comandos:
```
$ mvn clean
$ mvn package
```

### Gradle
Ingresamos a la carpeta **jacobitus4** y ejecutar los siguientes comandos:
```
$ ./gradlew clean jar
```

## Documentación de los servicios REST
```
$ apidoc -i src/main/java/bo/firmadigital/jacobitus4/resources/ -o src/main/resources/web/apidoc
```

## Generar instalador con compilación Maven

### Windows
```
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type msi --icon iconos/icon.ico -d dist --win-menu
```

### Linux
```
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type deb --icon iconos/icon.png -d dist
```

### MacOS
```
/Library/Java/JavaVirtualMachines/jdk-15.0.2.jdk/Contents/Home/bin/jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type pkg --icon iconos/icon.icns -d dist
```


## Generar instalador con compilación Gradle

### Windows
```
jpackage --input build/libs --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type msi --icon iconos/icon.ico -d dist --win-menu
```

### Linux
```
jpackage --input build/libs --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type deb --icon iconos/icon.png -d dist
```

### MacOS
```
/Library/Java/JavaVirtualMachines/jdk-15.0.2.jdk/Contents/Home/bin/jpackage --input build/libs --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type pkg --icon iconos/icon.icns -d dist
```
