# Gomaotsu otomeList Visualizer
Gomaotsu otomeList Visualizer will visualize your otome list.

![Otome network](./images/gomaotsu.png "Otome network")

## How to use this application
### Requirements
This application requires that your computer is connected to the internet.
I have tested this application with Java 8 (1.8.0_40) on MacOSX.

### Download and setup
#### Clone this project
```sh
cd ~/git/
git clone https://github.com/funasoul/gomaotsu.git
```

#### Build from command line
```sh
sudo port install maven32        # if your machine doesn't have Maven
sudo port select maven maven32   # if your machine doesn't have Maven
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
mvn clean package
```

#### Launch from command line
```sh
java -jar target/Gomaotsu-${version}-SNAPSHOT-jar-with-dependencies.jar [options...]
 -a (--add)    : always add 5 otome to graph (default: false)
 -g (--guild)  : generate graph for guild battle (default: false)
 -h (--help)   : display usage (default: false)
 -u (--update) : download and update friendlist from web (default: false)
```

Or, you can import this project to Eclipse as follows:
#### Launch Eclipse and import as Maven project.
1. [File] -> [Import] -> [Maven] -> [Existing Maven Project] -> [Next]
2. Navigate to ```~/git/gomaotsu``` -> [Next]
3. Select ```/pom.xml``` which you will see in the dialog.
4. Press [Next], then Eclipse will create a new project.
That's it!
