#!/bin/pwsh

$events = 250000
$lengths = @(0.03, 0.05, 0.07, 0.09)
$particles = @(200, 250, 300, 350, 400, 450)

$outputs = @(
    "src/main/python/simulations/steps",
    "src/main/python/simulations/events.txt",
    "src/main/python/simulations/setup.txt"
)

mvn clean compile assembly:single

while ($true) {
    $l = $lengths | Get-Random
    $p = $particles | Get-Random
    $t = [System.DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    java "-Duser.language=en" "-Duser.country=US" "-Duser.variant=US" -jar target/tp3-1.0.jar $events $l $p
    $id = "$l-$p-$t"
    echo "Simulation ID: $id"
    Compress-Archive -Path $outputs -DestinationPath "output/$id.zip"
}
