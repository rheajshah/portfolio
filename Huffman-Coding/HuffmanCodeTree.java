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

import java.io.IOException;

public class HuffmanCodeTree implements IHuffConstants{

    private TreeNode root; //tree that stores Huffman Tree
    PriorityQueue<TreeNode> huffmanPQ;

    //default constructor
    public HuffmanCodeTree() {
    }

    public HuffmanCodeTree(PriorityQueue<TreeNode> huffmanPQ) {
        this.huffmanPQ = huffmanPQ;
    }

    /*
    Returns root node of Huffman tree
     */
    public TreeNode getRoot(){
        return root;
    }

    /*
    Sets root node of Huffman tree
     */
    public void setRoot(TreeNode newRoot){
       root = newRoot;
    }

    /*
    Builds HuffmanTree using PQ

    Steps from slides:
        - Create new node
        – Dequeue node and make it left child
        – Dequeue next node and make it right child
        – Frequency of new node equals sum of frequency of
        left and right children
            - New node does not contain value
        – Enqueue new node back into the priority queue

     */
    public void buildHuffmanTree() {
        root = null;
        while (huffmanPQ.size() > 1) { //>= 2
            TreeNode leftChild = huffmanPQ.poll();
            TreeNode rightChild = huffmanPQ.poll();
            TreeNode parent = new TreeNode(leftChild, -1, rightChild); //new node aka parent
            //used -1 as a placeholder value, doesn't mean anything
            huffmanPQ.add(parent);
        }
        //once while loop is exited, aka PQ only has one TreeNode left = fully constructed Huffman Tree:
        root = huffmanPQ.poll(); //PQ should be empty after this, adds tree to class variable
    }

    /*
    Algo to reconstruct HuffmanTree
    Read in 32 bits (BITS_PER_INT) as the size of the tree
    Set the root equal to the result of the recursive method call

    Recursive helper:
    Read in 1 bit
    If the bit is 0:
        This is an internal node
        Make a new empty node
        Set the left child to result of call to recursive helper
        Set the right child to result of call the recursive helper
        Return the new node
    If the bit is a 1:
        This is a leaf node
        Read in 9 more bits (this is the value of the node)
        Make a new node w/ this value and no child nodes
        Return the new node
    Else:
    We ran out of bits while trying to form our huffman code tree
    This means something is incorrect about format of the input file
    Throw an exception/show an error/report catastrophic failure
     */
    public TreeNode reconstructHuffmanTree(TreeNode currNode, BitInputStream bis) throws IOException {
        int currBit = bis.readBits(1);
        if(currBit == 0){
            return new TreeNode(reconstructHuffmanTree(currNode.getLeft(), bis),
                    -1, reconstructHuffmanTree(currNode.getRight(), bis));
        } else if(currBit == 1){ //leaf node
            int valueOfNode = bis.readBits(BITS_PER_WORD + 1); //read 9 bits + converts to int value
            return new TreeNode(valueOfNode, 1); //no children -- frequency irrelevant now, just a random #
        } else{
           throw new IllegalArgumentException("ran out of bits  trying to form  huffman code tree");
        }
    }
}
