# Status test.
I definitions.inc
> init
<<<
+ test1.txt test1.txt
> add test1.txt
<<<
+ test2.txt test2.txt
> add test2.txt
<<<
+ test3.txt test3.txt
> add test3.txt
<<<
> commit "tests 1 2 and 3"
<<<
> status
=== Branches ===
*master
=== Staged Files ===
=== Removed Files ===
=== Modifications Not Staged For Commit ===
=== Untracked Files ===
<<<
> branch branch1
<<<
> checkout branch1
<<<
+ test1.txt test1v2.txt
> add test1.txt
<<<
> rm test2.txt
<<<
> commit "tests 1 and 3 + removed test 2"
<<<
* test2.txt
> checkout master
<<<
= test2.txt test2.txt
= test1.txt test1.txt
> checkout branch1
<<<
= test1.txt test1v2.txt
* test2.txt
