# TextGammaTool

This program implements text-gamma, an inter-annotator agreement measure for
categorization with simultaneous segmentation and transcription-error
correction.

text-gamma is described in the following paper:

Fabian Barteld, Ingrid Schröder and Heike Zinsmeister (2016): 
text-gamma – Inter-annotator agreement for categorization with simultaneous segmentation and transcription-error correction.
In: Proceedings of the 13th Conference on Natural Language Processing (KONVENS 2016), 27-37.
[https://www.linguistics.rub.de/konvens16/pub/4_konvensproc.pdf](https://www.linguistics.rub.de/konvens16/pub/4_konvensproc.pdf).

This is the version of the TextGammaTool that has been used for the evaluation
of text-gamma presented in the paper. In its current state, the program contains
three commands that can be run from the commandline. The commands read an
annotated text from an input file that is defined with 3 arguments:

1. The format of the input file.  
   Currenty only the XML format used by the annotation tool
   [CorA](https://www.linguistics.ruhr-uni-bochum.de/comphist/resources/cora/)
   is supported using the argument "CoraXML" or "CoraXMLReN".
2. Options for the input format.  
   "CoraXML" takes two obligatory options: the "mergeSymbol" and the "splitSymbol".  
   "CoraXMLReN" is a shortcut for "CoraXML" with the options "mergeSymbol=#,splitSymbol=§".
3. The file name of the input file.

TextGammaTool depends on
[Apache Commons Math](http://commons.apache.org/proper/commons-math/) 3.6.1 and
DKPro Agreement from
[DKPro Statistics](https://dkpro.github.io/dkpro-statistics/) 2.1.0. These
dependencies have to be present in the classpath.

The commands are:

- ui.TextGammaEvaluation  
  This command takes a text as input and does an evaluation using the corpus
  shuffling method described in the paper. It takes the three arguments
  described above and three further arguments that denote the characters used to
  encode the beginning and the end of units and gaps in the aligned texts. These
  three characters are not allowed to appear in the original text. The following
  example call has been used to run the evaluation for the paper:
  `java -Xmx15G -Xss10M -cp TextGammaTool-0.1.0.jar:lib/commons-math3-3.6.1.jar:lib/dkpro-statistics-agreement-2.1.0.jar ui.TextGammaEvaluation CoraXMLReN _ "inputfile.xml" « » ―`
- ui.EvaluateNumberOfAlignments  
  Reads a text, aligns it with results from applying the shuffling with the
  maximal values used for the evaluation and outputs the number of alignments
  with the minimal value for gamma.
  It takes the same parameters as TextGammaEvaluation.
- ui.CreateDataForGammaSoftware  
  Reads a text and outputs it and shuffled versions of the text in the format
  that the [gamma software](https://gamma.greyc.fr/) accepts as input.
  This has been used to create the data for the gamma evaluation.
  It takes only the three parameters described above.

We will soon add a user interface that allows to calculate text-gamma for two
annotated texts and get the resulting alignment.

If you use text-gamma for scientific work, please cite the paper mentioned above.
