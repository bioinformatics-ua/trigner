Trigner
=====

**Trigner** is an open source machine learning-based solution for biomedical event trigger recognition. It takes advantage of Conditional Random Fields (CRFs) with a high-end feature set, including linguistic-based, orthographic, morphological, local context and dependency parsing features. Additionally, a completely configurable algorithm is used to automatically optimize the feature set and training parameters for each event type, selecting the features that have a positive contribution and optimizing the CRF model order, n-grams sizes, vertex information and maximum hops for dependency parsing features. The final output consists of various CRF models, each one optimized to the linguistic characteristics of each event type.

Download
-------------

### Tool

[**Download tool**](http://bioinformatics.ua.pt/support/trigner/trigner.zip)


### Resources

[**Download resources**](http://bioinformatics.ua.pt/support/trigner/resources.zip) (
[GDep](http://bioinformatics.ua.pt/support/trigner/gdep.zip),
[Corpora](http://bioinformatics.ua.pt/support/trigner/corpora.zip),
[Models](http://bioinformatics.ua.pt/support/trigner/models.zip) and
[Dictionaries](http://bioinformatics.ua.pt/support/trigner/dictionaries.zip)
)


Documentation
-------------
The following utility scripts are provided:

* **convert.sh**: perform sentence splitting, tokenization, lemmatization, POS tagging, chunking and dependency parsing on input data in A1 format and store the resulting output in a compressed file;
* **train.sh**: train a model to recognize a specific or a set of event triggers;
* **optimize.sh**: find the optimal model configuration of a specific event trigger;
* **annotate.sh**: annotate a set of documents using a set of models and/or dictionaries;
* **evaluate.sh**: evalute the quality of the provided event triggers, which are provided in A1 format;
* **dictionary.sh**: generate dictionaries of event triggers based on input data in A1 format;
* **split.sh**: randomly split a corpus into two parts, following a provided ratio;
* **merge.sh**: combine multiple corpora files into a single corpus file.

Please use the option "-h" for further help on each script.

Bug tracker
-----------
Have a bug? Please create an issue here on GitHub!

[https://github.com/davidcampos/trigner/issues](https://github.com/davidcampos/trigner/issues)

Team
----
**David Campos**: david.campos(a)ua.pt

**Sérgio Matos**: aleixomatos(a)ua.pt

**José Luís Oliveira**: jlo(a)ua.pt


License
-------
Copyright (C) 2013 David Campos, Universidade de Aveiro, Instituto de Engenharia Electrónica e Telemática de Aveiro

Trigner is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of this license, visit [http://creativecommons.org/licenses/by-nc-sa/3.0/](http://creativecommons.org/licenses/by-nc-sa/3.0/).
