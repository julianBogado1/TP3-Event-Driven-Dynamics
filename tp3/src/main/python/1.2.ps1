param(
    [float]$1
)

cd ../../..
mvn clean compile assembly:single
java "-Duser.language=en" "-Duser.country=US" "-Duser.variant=US" -jar target/tp3-1.0.jar 250000 $1 250
cd -
py average.py
