# Jacobitus de Escritorio

Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos

- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior
- Git
- PC/SC para detección de tokens USB

Actualizamos el sistema operativo:
```bash
$ sudo pacman -Syu
```

### Instalación de Java 15

Descargamos el JDK desde el sitio oficial:

[OpenJDK Archive](https://jdk.java.net/archive/)

Instalamos utilidades necesarias:
```bash
$ sudo pacman -S --needed wget tar
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
$ sudo tar -zxf ./openjdk-15.0.2_linux-x64_bin.tar.gz -C /usr/lib/jvm
```

-:
```bash
$ nano ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/jdk-15.0.2
export PATH=$JAVA_HOME/bin:$PATH
$ source ~/.bashrc
```

Verificamos la instalación:
```bash
$ java -version
```

### Instalación g++

```bash
$ sudo pacman -S --needed base-devel
```

Verificamos la instalación:
```bash
$ g++ --version
```

### Instalación git

```bash
$ sudo pacman -S --needed git
```

Verificamos la instalación:
```bash
$ git --version
```

Habilitamos el cache de credenciales:
```bash
$ git config --global credential.helper cache
```

### Instalación y activación de PC/SC

Para utilizar tokens USB como ePass2003, instalamos el servicio PC/SC y el controlador CCID:

```bash
$ sudo pacman -S --needed pcsclite ccid pcsc-tools opensc usbutils
```

Habilitamos el socket de PC/SC para que se inicie automáticamente cuando la aplicación lo requiera:

```bash
$ sudo systemctl enable --now pcscd.socket
```

Verificamos que el servicio esté disponible:

```bash
$ systemctl status pcscd.socket
$ pcsc_scan
```

Si al ejecutar la aplicación aparece el error `SCARD_E_NO_SERVICE`, significa que `pcscd` no está activo o no está instalado.

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

Generamos el paquete:
```bash
$ ./gradlew packageArch
```
