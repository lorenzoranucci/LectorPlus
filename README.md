# LectorPlus
**Lector** is an extraction tool originating from a joint research project between Roma Tre University and University of Alberta. The tool is able to extract facts from English Wikipedia article text, learning the expressions that are commonly used to describe instances of relations between named entities in the text. It reaches an estimated precision of 95% in its first version. 

**LectorPlus** is an extension in which the tool has been applied to different languages. It is now able to extract facts for Spanish, Italian, French and German version of Wikipedia.

More information is available about the project at the Lector homepage: http://www.dia.uniroma3.it/db/lector/


## Getting Started

To execute LectorPlus on your machine you should have installed:
- JDK 1.8
- [Apache Maven](https://maven.apache.org/)
- command line tool:  **wget** and **git**

### Clone the project

First of all, clone the project in your local folder using:
```
git clone https://github.com/miccia4/LectorPlus.git
```

### Setting up the environment

The tool takes as input a Wikipedia (XML) (in one of the language above) and outputs a NTriples file with the triples that have been extracted. 

- In order to run the tool on a specific version of Wikipedia (XML) dump please edit the file:
	 ```
	 dumps.properties
	 ```
	it lists the specific URLs of the input Wikipedia dump. We filled it with the complete dumps of Feb. 2017 but other versions can be easily used (from https://dumps.wikimedia.org/).

- Also, in order to simplify the download of those dumps and the picking up of the other necessary files we provide a script which creates the folders and set up the file system used by LectorPlus. 
	
	Run once our install script:
	```
	sh install.sh
	```
	It will take some time (many files to downlaod) but at the end it will create the root folder `/data` described below.

#### Structure of the folder `/data`
The folder `/data` contains a list of sub-folders and includes all the necessary files:

	|-- input (en es it de fr):									
	|		|-- wikipedia: it contains the XML dump of Wikipedia
	|		|-- dbpedia: it contains the Mappingbased Objects dump of DBPedia
	|
	|-- languages: it contains the properties used by the parser
	|
	|-- lists (en es it de fr): used by the parser to filter out undesired NE
	|		|-- currencies.tsv
	|		|-- nationalities.tsv
	|		|-- professions.tsv											|				
	|-- models (en): OpenNLP models that are used from the English parser.
	|		|-- en-lemmatizer.dict
	|		|-- en-pos-maxent.bin
	|		|-- en-token.bin
	|
	|-- sources (en es it de fr): other important files used in the process
	|		|-- type
	|		|-- redirect.tsv

Other folders are created at run-time:

	|-- index: it will contains the Lucene index of DBPedia MappingBased objects, redirects and types
	|
	|-- lector: it will contains a csv file with the phrases used by LectorPlus, for each language
	
### Build and run

After created the folder `/data` you are ready to build a executable version of LectorPlus, using:
```
maven clean install
```

and running it using the following command:

```
sh run.sh <output_folder>
```
It takes the path of the output folder as a parameter and executes the extraction from all the Wikipedia dumps listed in `dumps.properties` file.
The output folder will contain all the compressed NTriples files produced by the tool.


## Details and contacts
More details can be found in the paper:

>  "Accurate Fact Harvesting from Natural Language Text in Wikipedia with Lector."   
>  by Matteo Cannaviccio, Denilson Barbosa and Paolo Merialdo.   

The paper was presented at the "19th International Workshop on the Web and Databases (WebDB 2016)" 
(http://webdb2016.technion.ac.il/program.html).

If you have any questions, please feel free to contact the authors.

- Matteo Cannaviccio (cannaviccio@uniroma3.it)
- Denilson Barbosa (denilson@ualberta.ca)
- Paolo Merialdo (merialdo@uniroma3.it)
