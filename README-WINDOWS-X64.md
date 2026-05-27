# Jacobitus Escritorio

Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos

- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior
- Git

### Instalación de Java 15

Descargamos el JDK desde el sitio oficial:

[OpenJDK Archive](https://jdk.java.net/archive/)

Descargamos OpenJDK 15.0.2:
- Versión para [Windows de 64 bits](https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_windows-x64_bin.zip).

Descomprimmos el archivo **openjdk-15.0.2_windows-x64_bin.zip** en la carpeta **C:\Java**.

Creamos la variable de entorno **JAVA_HOME** con el valor **C:\Java\jdk-15.0.2**.

Actualizamos la variable de entorno **PATH** con el valor **%JAVA_HOME%\bin**.

Verificamos la instalación:
```bash
java -version
```

### Instalación g++

Descargamos el instalador de **MSYS2** (https://www.msys2.org/) desde el siguiente enlace:

- https://github.com/msys2/msys2-installer/releases/download/2024-07-27/msys2-x86_64-20240727.exe

Ejecutamos el instalador **msys2-x86_64-20240727.exe**, el directorio de instalacion por defecto es **C:\msys64**.

Ejecutamos **MSYS64** al terminar la instalación.

En la terminal de **MinGW-w64 toolchain** ejecutamos:
```bash
pacman -Syu
pacman -S --needed base-devel mingw-w64-ucrt-x86_64-toolchain
```

Seleccionamos la opción por defecto y procedemos con la instalación.

Actualizamos la variable de entorno **PATH** con el valor **C:\msys64\ucrt64\bin**.

Verificamos la instalación:
```bash
g++ --version
```

### Instalación git

Descargamos en instalador desde la dirección:

- https://git-scm.com/download/win

Ejecutamos el instalador.

Verificamos la instalación:
```bash
git --version
```

Habilitamos el cache de credenciales:
```bash
$ git config --global credential.helper cache
```

## Compilación del proyecto

Clonamos el proyecto desde el Repositorio Estatal de Software Libre:
```bash
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus-total/jacobitus-escritorio.git --recursive
```

Ingresamos al directorio:
```bash
$ cd jacobitus-escritorio
```

### Ejecución de la aplicación

Compilamos:
```bash
$ ./gradlew jacobitus-libreria:buildNative escritorio:clean escritorio:importCA escritorio:addDrivers escritorio:importChangePin escritorio:shadowJar
```

Ejecutamos:
```bash
$ ./gradlew escritorio:run
```

## Generación del paquete de instalación

Para la generación de un archivo msi/exe es necesario:
- WiX 3.11.x

Generamos el instalador:
```bash
$ ./gradlew packageApp
```
