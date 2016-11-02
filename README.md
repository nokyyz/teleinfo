# Teleinfo Reader

**Teleinfo Reader** est une application qui permet :
* de **lire** les données Teleinfo de votre compteur ERDF/EDF depuis votre port série
* d'**historiser** l'ensemble des données Teleinfo lues
* de **diffuser** en temps réel et/ou à la demande ces données Teleinfo.

<img src="./doc/resources/generale_architecture.png" alt="Générale architecture" style="width: 400px;"/>

## Fonctionnalités

### Lecture des données Teleinfo
Teleinfo Reader permet de lire et décoder les données Teleinfo depuis le port série de votre système d'exploitation (ex : port COM sous Windows, /dev/ttyAxxx depuis Linux).

Les données Teleinfo prises en compte sont :
* les trames de base
* les trames Heures creuses / Heures pleines
* les trames Tempo
* les trames EJP

### Historisation des données Teleinfo
Teleinfo Reader stocke l'ensemble des trames Teleinfo lues. Ces données sont disponibles pour la Diffusion à  la demande.

Teleinfo Reader supporte plusieurs mécanismes de persistence. En effet, la persistence des données est gérée sous forme d'addon, ce qui permet d'ajouter de nombreuses implémentations.   

### Diffusion des données Teleinfo

#### Diffusion "temps réel"

#### Diffusion à la demande

## Pré-requis

### Installation matérielle 
La lecture de données Teleinfo nécessite un montage électronique afin de récupérer les trames Teleinfo d'un compteur ERDF compatible. De nombreux montages électroniques sont proposés sur Internet. Pour ma part, j'ai choisi l'excellente solution de *Magdiblog* basée sur le port GPIO d'un Raspberry PI ; le tutoriel est disponible à l'adresse suivante : <http://www.magdiblog.fr/gpio/teleinfo-edf-suivi-conso-de-votre-compteur-electrique/>.
Teleinfo Reader vous permettra alors de décoder les trames reçues par votre port série afin de les historiser et/ou les diffuser à vos autres logiciels (ex : [OpenHAB](http://www.openhab.org), [Jeedom](https://www.jeedom.com), etc.).

### Système d'exploitation
Teleinfo Reader s'exécute sur l'ensemble des OS supportés par le langage **Java 1.7** et ultérieur : Windows, Linux et MacOS.
Teleinfo Reader embarque sa propre JVM afin de vous proposez une solution "Out-of-Box". Il vous est toutefois possible d'installer et d'utiliser une version différente de JVM. Pour définir une autre JMV/JRE, veuillez vous référer au chapitre **TBD**.

## Le projet
Ce projet a initialement été développé dans le but de fournir une extension Teleinfo au logiciel domotique [OpenHAB.org](http://www.openhab.org). 
De part sa modularité et l'utilisation de technologies interopérables, Teleinfo Reader est utilisable dans de nombreux autres contextes.


## Concepts


## Comment utiliser Teleinfo Reader


## Extensions

## Roadmap
