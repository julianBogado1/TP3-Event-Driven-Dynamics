# Presentación:

## IMPORTANTES



### Modelo:

 - [ ] Bien el modelo general. Como comentario, en la sección de modelo debería decir "arcotengente" en lugar de "atan2". El modelo utiliza el arcotangente, la función específica que hayan utilizado es parte de implementación. 

 - [ ] Faltó la definición del modelo votante 



 - [ ] La sección simulaciones no se corresponde con los resultados mostrados. En D14 dicen que usaron N=600, y en los resultados dicen N=1000. 

 - [ ] No estan especificados los rangos de estudio para vicsek

---

### Estructura de resultados:

 - [ ] No deben mostrarse todas las animaciones, luego todas las evoluciones temporales y finalmente todos los resultados de respuesta a cada parámetro. El análsis debe ser ordenado por cada estudio.

 - [ ] Lo correcto sería seguir ese orden pero por cada análsis, por ejemplo:



<details><summary>Orden correcto</summary>

1) Estudio de ruido:

a) Animacion ruido bajo

b) Animación ruido alto



Evoluciones temporales para ruido (mostrando desde dónde es el estacionario)



Respuesta del observable (v_a) a la variación de ruido.



2) Estudio de densidad:

a) Animacion Densidad alta

b) Animación Densidad baja



Evoluciones temporales para la densidad



Respuesta de v_a a la variación de densidad



3) Estudio del modelo votante ....

</details>


Si no se hace esto, es difícil/imposible seguir el hilo de la presentación. 


 - [ ] Las barras de error no se corresponden con lo mostrado. Ej: En la D23 se ve que el valor oscila entre 0.6 y casi 0.9 oproximadamente, pero para la configuración mostrada, en D24 se ve una barra minúscula. 



 - [ ] No se muestra desde dónde se consideró estacionario ni se explicó cómo se realizó el promedio de las repeticiones. 

---

### Conclusiones:

 - [ ] La idea de punto crítico no se corresponde con que la transición sea continua. Para mencionar la existencia de un punto crítico, tendrían que mostrar que mientras más grande es el sistema, más abrupta es la transición. Como ese análsis no se hizo, no es una conclusión válida, ni en el average ni en el de votante.



 - [ ] Las conclusiones relacionadas al timepo de consenso no se corresponden con la figura mostrada. 

---

### En general, faltó revisar y pulir la presentación antes de entregar. 

 - [ ] Las diapositivas 16,17 y 18 son las mismas que 19, 20 y 21, pero con peor calidad; hay figuras que no se ajustan a la diapositiva (19,20,23) y números de diapositiva repetidos (19, 20 y 21).  

---

### Correciones de formato:



 - [ ] Los títulos de sección no necesitan subtítulos. Además, los que utilizaron son erróneos.

 - [ ] "Observables" no es parte de modelo matemático. 

 - [ ] "Parametros de ejecución" no va en resultados.



 - [ ] Es importante mantener el uso de los símbolos y nombres. 

<details><summary>Ejemplo</summary>

Por ejemplo, para el observable para el parámetro de orden "v_a" usaron:

Parametro de orden

v_a 

Orden

Interacción

Y para la densidad usaron diferentes tipos de "rho". 

</details>


 - [ ] Para permitir una comparación correcta, deben fijar los valores mínimos y máximos del eje "Y", y no dejar la asignación dinámica. Para el caso de v_a, simepre usar de 0 a 1. De lo contrario se exageran las variaciones artificialmente, como ocurre en la D20.



 - [ ] Los valores en los ejes son muy pequeños (deben ser similares al resto del texto de la diapositiva) y no se debe usar notación científica si no es necesario. "0.5" es preferible a "5x10⁻¹".  



 - [ ] En D19 y 20, en el eje "Y" el nombre no debería ser "orden". El símbolo de ese obserbable es "v_a" y su nombre en el articulo original es polarización.



# Informe:

 - [ ] Valen para el informe las correcciones de la presentación que sean aplicables al informe.

---

### Orden y secciones:



 - [ ] La seccion modelo tiene partes que correponden a simulaciones. La descripción del sistema particular (LxL y contornos periódicos). Aquí sólo van las ecuaciones de movimiento y actualizaciones.



 - [ ] El gráfico 6.2 podria estar directamente en la sección de implementación y no en anexo. 

---

### Resultados:

 - [ ] Los captions de las figuras 1, 2 y 3 son insuficientes. Es necesario decir con qué parametros se generó, si es un fotograma una vez alcanzado en estacionario o no, y la interpretacion de los colores, aunque la barra al costado lo aclare. Solo como recomentación, podrían poner varias en una sola figura como sub-figuras, y hacer referencia a ellas como figura 1A, 1B, etc.



 - [ ] Recomendable forzar las figuras a estar en la sección-subsección que las mencione primero. En la estructura presentada, algunas figuras correspondientes al anásisis de la amplitud de ruido están en la sección en que se habla del análisis de densidad. 

---

### Sección 5.1.3. Curva η vs. va



 - [ ] La frase "[...] con barras de error obtenidas de realizaciones independientes". No es suficiente para entender qué se promedió. Al tener varias realizaciones se trata de un promedio de promedios. Es necesario describir si se calcularon primero promedios temporales y luego se promediaron las medias obtenidas (se subestima el error) o si se realizó un único promedio con todos los datos del estacionario juntos (correcto).

---

 - [ ] Análisis de tiempo de consenso:

## Importante:

 - [ ] Para este análisis, es aún más importante mostrar exactamente como se encontró el tiempo de consenso, dado que es el valor analizado. 



 - [ ] Mencionan que se varía la densidad y el tamaño del recinto, cuando al tener N fijo, estas dos variables son la misma. 



 - [ ] Hay contradicciones entre lo que dicen en la sección simulaciones: 

"En esta instancia, el estudio de ρ se lleva a cabo variando el numero de particulas en el rango [500; 15000], con un valor fijo de η = 0,15."

Más adelante en resultados:

"Se fijó la cantidad de partı́culas en N=600 y se variaron la densidad, módulo de la velocidad ..."



 - [ ] El análisis que hacen del comportamiento no se corresponde con lo que muestra la Figura 12. Para la curva de v=0.03, el mínimo podría encontrarse en la mínima densidad. Además, los puntos mostrados no son suficientes para decir que convergen en "y=2N".
