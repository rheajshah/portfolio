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
> rm test2.txt
<<<
> status
=== Branches ===
*master
=== Staged Files ===
=== Removed Files ===
test2.txt
=== Modifications Not Staged For Commit ===
=== Untracked Files ===
<<<
+ test1.txt test1v2.txt
> add test1.txt
<<<
> status
=== Branches ===
*master
=== Staged Files ===
test1.txt
=== Removed Files ===
test2.txt
=== Modifications Not Staged For Commit ===
=== Untracked Files ===
<<<
