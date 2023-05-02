/*  Student information for assignment:
 *
 *  On <MY> honor, <Rhea Shah>, this programming assignment is <MY> own work
 *  and <I> have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: rjs4665
 *  email address: rheajshah@gmail.com
 *  Grader name: Emma Simon
 *
 *  Student 2
 *  UTEID: -
 *  email address: -
 */

Q&A:
1. What kinds of file lead to lots of compressions?
    Files with much repeating detail, such as black and white images or text files with
    repeated characters lead to lots of compression. Since there are fewer characters present
    within the file at a high frequency each, the resulting Tree has few nodes and short codes,
    leading to a lot of compression in the .hf file.
2. What kind of files had little or no compression?
    Files that lead to little or no compression are high resolution files that contain
    lots of detail and not much repetition. For instance, the Waterloo test folder containing
    TIF files - high quality photos (containing multiple hues).
    Since these files contain multiple different characters all with relatively low frequencies,
    there are multiple nodes in the Huffman encoding tree, and each code will be considerably
    long, not leading to much compression.
3. What happens when you try and compress a huffman code file?
   Results from compressing all .hf files in BooksAndHTML:
        total bytes read: 11644696
        total compressed bytes 11567484
        total percent compression 0.663
        compression time: 38.480
   Trying to compress a huffman code file leads to very little compression as can be seen
   by the results above (only 0.663% compression).

HuffMark Results:
Calgary:
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper6.hf
paper6 from	 38105 to	 25057 in	 0.113
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper1.hf
paper1 from	 53161 to	 34371 in	 0.123
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/obj1.hf
obj1 from	 21504 to	 17085 in	 0.066
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/geo.hf
geo from	 102400 to	 73592 in	 0.251
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/progc.hf
progc from	 39611 to	 26948 in	 0.098
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/progl.hf
progl from	 71646 to	 44017 in	 0.156
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/book1.hf
book1 from	 768771 to	 439409 in	 1.475
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/progp.hf
progp from	 49379 to	 31248 in	 0.142
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/pic.hf
pic from	 513216 to	 107586 in	 0.389
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/news.hf
news from	 377109 to	 247428 in	 0.844
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper4.hf
paper4 from	 13286 to	 8894 in	 0.036
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/bib.hf
bib from	 111261 to	 73795 in	 0.234
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper3.hf
paper3 from	 46526 to	 28309 in	 0.126
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper2.hf
paper2 from	 82199 to	 48649 in	 0.185
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/paper5.hf
paper5 from	 11954 to	 8465 in	 0.038
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/obj2.hf
obj2 from	 246814 to	 195131 in	 0.646
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/trans.hf
trans from	 93695 to	 66252 in	 0.226
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/calgary/book2.hf
book2 from	 610856 to	 369335 in	 1.527
--------
total bytes read: 3251493
total compressed bytes 1845571
total percent compression 43.239
compression time: 6.675


Waterloo:
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/sail.tif.hf
sail.tif from	 1179784 to	 1085501 in	 3.801
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/monarch.tif.hf
monarch.tif from	 1179784 to	 1109973 in	 3.538
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/clegg.tif.hf
clegg.tif from	 2149096 to	 2034595 in	 5.986
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/lena.tif.hf
lena.tif from	 786568 to	 766146 in	 2.302
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/serrano.tif.hf
serrano.tif from	 1498414 to	 1127645 in	 3.352
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/peppers.tif.hf
peppers.tif from	 786568 to	 756968 in	 2.308
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/tulips.tif.hf
tulips.tif from	 1179784 to	 1135861 in	 3.379
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/waterloo/frymire.tif.hf
frymire.tif from	 3706306 to	 2188593 in	 6.520
--------
total bytes read: 12466304
total compressed bytes 10205282
total percent compression 18.137
compression time: 31.186

BooksAndHTML:
melville.txt from	 82140 to	 47364 in	 0.191
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/A7_Recursion.html.hf
A7_Recursion.html from	 41163 to	 26189 in	 0.113
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/jnglb10.txt.hf
jnglb10.txt from	 292059 to	 168618 in	 0.588
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/ThroughTheLookingGlass.txt.hf
ThroughTheLookingGlass.txt from	 188199 to	 110293 in	 0.409
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/syllabus.htm.hf
syllabus.htm from	 33273 to	 21342 in	 0.085
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/revDictionary.txt.hf
revDictionary.txt from	 1130523 to	 611618 in	 2.604
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/CiaFactBook2000.txt.hf
CiaFactBook2000.txt from	 3497369 to	 2260664 in	 9.012
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/melville.txt.unhf.hf
melville.txt.unhf from	 82140 to	 47364 in	 0.176
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/kjv10.txt.hf
kjv10.txt from	 4345020 to	 2489768 in	 8.639
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/rawMovieGross.txt.hf
rawMovieGross.txt from	 117272 to	 53833 in	 0.203
compressing to: /Users/rheashah/Documents/CS314_Scott/assgn10/BooksAndHTML/quotes.htm.hf
quotes.htm from	 61563 to	 38423 in	 0.135
--------
total bytes read: 9870721
total compressed bytes 5875476
total percent compression 40.476
compression time: 22.155