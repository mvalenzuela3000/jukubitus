# Jacobitus Escritorio
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior

### Instalación de Java 15

Descargamos el JDK desde el sitio web https://jdk.java.net/archive/

- Versión para [Windows](https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_windows-x64_bin.zip).


Descomprimmos el archivo **openjdk-15.0.2_windows-x64_bin.zip** en la carpeta **C:\Java**

Creamos la variable de entorno **JAVA_HOME** con el valor **C:\Java\jdk-15.0.2**

Actualizamos la variable de entorno **PATH** con el valor **%JAVA_HOME%\bin**

Verificamos la instalación.
```bash
java -version
```

### Instalación gradle

Descargamos gradle desde el siguiente enlace:

- https://services.gradle.org/distributions/gradle-7.6.2-bin.zip

Descomprimimos el archivo **gradle-7.6.2-bin.zip** en la carpeta **C:\Gradle**

Creamos la variable de entorno **GRADLE_HOME** con el valor **C:\Gradle\gradle-7.6.2**

Actualizamos la variable de entorno **PATH** con el valor **%GRADLE_HOME%\bin**

Verificamos la instalación.
```bash
gradle --version
```

### Instalación g++

#### Instalamos **MinGW-w64**

Descargamos el instalador de **MSYS2** (https://www.msys2.org/) desde el siguiente enlace:

- https://github.com/msys2/msys2-installer/releases/download/2024-07-27/msys2-x86_64-20240727.exe

Ejecutamos el instalador **msys2-x86_64-20240727.exe**, el directorio de instalacion por defecto es **C:\msys64**

Ejecutamos **MSYS64** al terminar la instalación.

En la terminal de **MinGW-w64 toolchain** ejecutamos:
```bash
pacman -S --needed base-devel mingw-w64-ucrt-x86_64-toolchain
```

Seleccionamos la opción por defecto y procedemos con la instalación.

Actualizamos la variable de entorno **PATH** con el valor **C:\msys64\ucrt64\bin**

Verificamos la instalación.
```bash
gcc --version
g++ --version
gdb --version
```

### Instalación git

Descargamos en instalador desde la dirección:

- https://git-scm.com/download/win

Ejecutamos el instalador.

Verificamos la instalación.
```bash
git --version
```

Habilitamos el cache de credenciales para facilitar el manejo de submódulos
```bash
$ git config --global credential.helper cache
```

## Compilación del proyecto

Clonamos el proyecto desde el Repositorio Estatal de Software Libre.
```
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus-total/jacobitus-escritorio.git --recursive
```

### Gradle
Ingresamos a la carpeta **jacobitus-escritorio** y ejecutar los siguientes comandos:


```
$ ./gradlew jacobitus-libreria:buildForWindows escritorio:clean escritorio:importCA escritorio:importDrivers escritorio:importChangePinWindows escritorio:jar
$ ./gradlew escritorio:run
```

## Generar paquete de instalación

Para la generación de un archivo msi/exe es necesario:

- WiX 3.11.x

Para la generación de un archivo msi es necesario ejecutar:
```
$ ./gradlew packageMsi
```
