# Jacobitus de Escritorio

Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos

- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior
- Git

Actualizamos el sistema operativo:
```bash
$ sudo dnf update -y
```

### Instalación de Java 15

Descargamos el JDK desde el sitio oficial:

[OpenJDK Archive](https://jdk.java.net/archive/)

Instalamos utilidades necesarias:
```bash
$ sudo dnf install -y wget tar
```

Descargamos OpenJDK 15.0.2:
```bash
$ wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_linux-x64_bin.tar.gz
```

Creamos el directorio para JVMs si no existe:
```bash
$ sudo mkdir -p /usr/lib/jvm
```

Descomprimimos el archivo **openjdk-15.0.2_linux-x64_bin.tar.gz** en la carpeta **/usr/lib/jvm**:
```bash
$ sudo tar -zxf openjdk-15.0.2_linux-x64_bin.tar.gz -C /usr/lib/jvm
```

Registramos la versión de **java**:
```bash
$ sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-15.0.2/bin/java 1502
```

Registramos la versión de **javac**:
```bash
$ sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-15.0.2/bin/javac 1502
```

Establecemos Java 15 como versión por defecto:
```bash
$ sudo update-alternatives --config java
```

Seleccionamos la versión que se encuentra en la ruta **/usr/lib/jvm/jdk-15.0.2/bin/java**.

Verificamos la instalación:
```bash
$ java -version
```

### Instalación g++

```bash
$ sudo dnf groupinstall -y "Development Tools"
```

Verificamos la instalación:
```bash
$ g++ --version
```

### Instalación git

```bash
$ sudo dnf install -y git
```

Verificamos la instalación:
```bash
$ git --version
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

Instalamos rpmbuild:
```bash
$ sudo dnf install -y rpm-build rpmdevtools
```

Generamos el paquete:
```bash
$ ./gradlew packageRpm
```
