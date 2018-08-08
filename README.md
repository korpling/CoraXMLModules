![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# pepperModules-CoraXMLModules
This project provides an importer to support CoraXML in the linguistic converter framework Pepper (see https://u.hu-berlin.de/saltnpepper).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-pepperModules-CoraXMLModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The CoraXMLImporter is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<importer name="CoraXMLImporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="coraXML" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties
```xml
<importer name="CoraXMLImporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</property>
</importer>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/). 

## License
  Copyright 2013 Humboldt-Universität zu Berlin, INRIA.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

# CoraXMLImporter

This module allows to import the XML files produced by the annotation tool CorA
(https://cora.readthedocs.io/en/latest/coraxml/).

## Properties

The table contains an overview of all usable properties to customize the behavior of this Pepper module.

| Name of property	        | Type of property	 | optional/ mandatory | 	default value |
|-----------------------------|----------------------|---------------------|-------------------|
| dipl                        | String               | optional            | dipl              |
| mod                         | String               | optional            | mod               |
| exclude\_from\_import       | String               | optional            |                   |
| export.boundary\_tags       | String               | optional            | boundary          |
| export.create\_reference    | Boolean              | optional            | false             |
| export.layer\_for\_comments | String               | optional            |                   |
| export.subtoken\_markup     | String               | optional            |                   |
| export.token                | Boolean              | optional            | true              |
| mod/dipl\_eq\_token         | Boolean              | optional            | false             |
| tok.prefix                  | String               | optional            |                   |
| toktext.dipl                | String               | optional            | ascii             |
| toktext.mod                 | String               | optional            | utf               |

### dipl

Name of the XML elements that represent diplomatic tokens
(https://cora.readthedocs.io/en/latest/document-model/#tokenization).

### mod

Name of the XML elements that represent modernized tokens
(https://cora.readthedocs.io/en/latest/document-model/#tokenization).

### exclude\_from\_import

A list of annotypes (child elements of modernized tokens of the form
`<{annotype} tag="value"/>`) that are ignored for the import. The annotypes are
separated by `;`.

### export.boundary\_tags

CorA does not support span annotations but allows to set boundary flags
(https://cora.readthedocs.io/en/latest/layers/#boundaries) to mark the
boundaries of spans. Annotations of this type are converted into spans between
two boundaries for the import.

### export.create\_reference

If false, a separate span for each of the layout elements _page_, _column_ and
_line_ (https://cora.readthedocs.io/en/latest/document-model/#layout) is
created. If true, only spans for lines are created containing the complete
reference information, i.e. the page, the column and line.

### export.layer\_for\_comments

This property defines the tokenization layer into which token-level comments
(https://cora.readthedocs.io/en/latest/document-model/#token-level-comments) are
imported. If this property is left empty, token-level comments are omitted from
the import.

### export.subtoken\_markup

CorA does not allow to annotate subtoken spans. However, the transcription of
the tokens may contain arbitrary markup. This property allows to select a parser
that converts markup into span annotations.

Currently, only a parser for the markup used for the Reference corpus Middle Low
German / Low Rhenish
(https://corpora.uni-hamburg.de/hzsk/de/islandora/object/file:ren-0.7_transkriptionshandbuch/datastream/PDF/transkriptionshandbuch-0.7.pdf)
is provided. This parser can be chosen by setting the property to 'REN'.

### export.token

This property defines whether or not to export the virtual tokens
(https://cora.readthedocs.io/en/latest/document-model/#tokenization).

### mod/dipl\_eq\_token

CorA assumes that diplomatic and modernized tokens are purely tokenizations of
the the virtual tokens, i.e. the concatenation of the respective
trans-attributes should be the same (https://cora.readthedocs.io/en/latest/document-model/#tokenization).

However, this equality is not enforced in the document model, hence it can be
the case that it does not hold. Therefore, by default, the importer aligns
diplomatic and modernized tokens simply by their order, i.e., the first
diplomatic token is aligned with the first modernized token, and so on.
This can result in unintuitive alignments as in the following example.

<table>
    <thead>
    <tr>
        <th><i>token</i></th><th colspan="2">oberczugemich</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><i>dipl</i></td><td>ober</td><td>czugemich</td>
    </tr>
    <tr>
        <td><i>mod</i></td><td>oberczuge</td><td>mich</td>
    </tr>
    <tr>
        <td><i>tok</i></td><td></td><td></td>
    </tr>
    </tbody>
</table>

If you know that the documents that in the documents that you are importing the
difference between diplomatic and modernized tokens is actually one of
tokenization, you can set this property to 'true'.
In this case the importer will try to use the transcription to align the
diplomatic and modernized tokens as in the following example.

<table>
    <thead>
    <tr>
        <th><i>token</i></th><th colspan="3">oberczugemich</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><i>dipl</i></td><td>ober</td><td colspan="2">czugemich</td>
    </tr>
    <tr>
        <td><i>mod</i></td><td colspan="2">oberczuge</td><td>mich</td>
    </tr>
    <tr>
        <td><i>tok</i></td><td></td><td></td><td></td>
    </tr>
    </tbody>
</table>

The importer does not check for the equality of the concatenation and might fail
if the transcriptions differ.

### tok.prefix

Allows to define a prefix that is added to the names of the diplomatic and
normalized tokens.

### toktext.dipl

Define which representation
(https://cora.readthedocs.io/en/latest/document-model/#token-representations) of
the diplomatic tokens is imported: 'trans' or 'utf'.

### toktext.mod

Define which representation
(https://cora.readthedocs.io/en/latest/document-model/#token-representations) of
the diplomatic tokens is imported: 'trans', 'utf' or 'ascii'.
