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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int[] frequencies; //freq of each char in the file (256 possible chars)
    private HuffmanCodeTree hct;
    //private TreeNode root; //tree that stores Huffman Tree
    private Map<Integer, String> encodedMap;
    private int headerFormat; //SCF or STF
    private int compressedNumBits;
    private int originalNumBits;
    private int bitsSaved; //bits saved by performing compression

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it in one as needed.
     *
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     *                     header to use, standard count format, standard tree format, or
     *                     possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        //(re)set all class variables
        this.headerFormat = headerFormat;
        compressedNumBits = 0;
        originalNumBits = 0;
        bitsSaved = 0;

        //build frequency array
        BitInputStream bis = new BitInputStream(in);
        int currByteValue = bis.readBits(BITS_PER_WORD);
        frequencies = new int[ALPH_SIZE];
        while (currByteValue != -1) {
            frequencies[currByteValue]++;
            currByteValue = bis.readBits(BITS_PER_WORD);
        }
        bis.close();

        this.hct = new HuffmanCodeTree(buildHuffmanPQ());
        hct.buildHuffmanTree();
        buildHuffmanMap();

        //# bits in the original file - the number of bits written to the compressed file
        // (including the Huffman magic number, the header constant, the header, the data,
        // and the pseudo-eof, but NOT any padding added by the file system to get the file size
        // up to a multiple of 8.)
        originalNumBits = calculateNumBitsInOriginalFile();

        //bits in data, includes pseudo-eof #bits
        compressedNumBits = calculateNumBitsOfDataAfterCompression();
        compressedNumBits += BITS_PER_INT; //magic number (int = 32 bits)
        compressedNumBits += BITS_PER_INT;  //header format int (STC or STF) =  32 bits

        /*
        Don't include the PEOF frequency in the store counts format because it will always have a
        frequency of 1. Manually add this frequency to the priority queue yourself when
        decompressing or you will not generate the proper codes
         */
        if (headerFormat == STORE_COUNTS) { //standard count format
            //header stores freq of each value 0-255 (does not store eof = 256)
            compressedNumBits += ALPH_SIZE * BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) { //standard tree format
            //([num leaf nodes * 9] + size of tree) = 32 + tree size
            compressedNumBits += BITS_PER_INT + calculateBitsInTree(hct.getRoot());
        } else if (headerFormat == STORE_CUSTOM) { //custom format, dw about for test cases
            myViewer.showError("Header format not recognized/ implemented.");
        }

        bitsSaved = originalNumBits - compressedNumBits;
        return bitsSaved;
    }

    //it'll be stored with non-leaf = 1 bit (0) and leaf = 1 bit + 9 bits of binary value of the
    //int/value at node doesn't store frequency, b/c tree is already constructed
    private int calculateBitsInTree(TreeNode currNode) {
        if (currNode.isLeaf()) { //base case
            return 1 + BITS_PER_WORD + 1; //storing as bit rep 1 + value of the node
        } else {
            // 1 is for the bit rep 0
            return 1 + calculateBitsInTree(currNode.getLeft())
                    + calculateBitsInTree(currNode.getRight());
        }
    }

    /*
    Returns number of bits in the original file before compression
     */
    private int calculateNumBitsInOriginalFile() {
        int numBits = 0;
        for (int i = 0; i < ALPH_SIZE; i++) {
            numBits += frequencies[i] * BITS_PER_WORD;
        }
        return numBits;
    }

    /*
    Returns number of bits in the file after compression
     */
    private int calculateNumBitsOfDataAfterCompression() {
        int numBits = 0;
        for (Integer key : encodedMap.keySet()) {
            if (key != ALPH_SIZE) {
                String encodedValue = encodedMap.get(key);
                int numBitsInEncodedValue = encodedValue.length();
                //number of compressed bits for a value * num of times that value occurs
                //ex. E: 101 --> length = 3 * occurs 5 times = 15 bits
                numBits += numBitsInEncodedValue * frequencies[key];
            } else {
                numBits += encodedMap.get(ALPH_SIZE).length(); //pseudo-eof # of bits
            }
        }
        return numBits;
    }

    /*
    Builds Map containing huffman codes
     */
    private void buildHuffmanMap() {
        Map<Integer, String> encodingMap = new HashMap<>();
        encodeCharRecurseTree(hct.getRoot(), encodingMap, "");
        encodedMap = encodingMap; //store in class variable
    }

    /*
    Don't have to return anything because map is directly modified.
    Huffman tree builds prefix free codes, each one is a unique leaf
     */
    private void encodeCharRecurseTree(TreeNode t, Map<Integer, String> map, String encoding) {
        if (!t.isLeaf()) { //have not reached end of a certain character's path
            encodeCharRecurseTree(t.getLeft(), map, encoding + "0"); //recurse left, +0
            encodeCharRecurseTree(t.getRight(), map, encoding + "1"); //recurse right, +1
        } else {
            map.put(t.getValue(), encoding); //value of leaf node = char, huffman encoding
        }
    }

    /*
    Builds priority queue with the priorities being the frequencies of each char
     */
    private PriorityQueue buildHuffmanPQ() {
        PriorityQueue<TreeNode> huffmanPQ = new PriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] != 0) { //only add to PQ if char occurs 1+ times in file
                TreeNode curr = new TreeNode(i, frequencies[i]); //value = 0-255, freq
                huffmanPQ.add(curr);
            }
        }
        huffmanPQ.add(new TreeNode(PSEUDO_EOF, 1)); //pseudo-eof = 256
        return huffmanPQ;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     *
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     *              If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        //only compress if bits saved by compression >= 0 or if force compression is enabled
        if(bitsSaved > 0 || (bitsSaved < 0 && force)){
            BitInputStream bis = new BitInputStream(in);
            BitOutputStream bos = new BitOutputStream(out);
            bos.writeBits(BITS_PER_INT, MAGIC_NUMBER); //32, magic number
            bos.writeBits(BITS_PER_INT, headerFormat); //32, current header format
            //write header content
            if (headerFormat == STORE_COUNTS) { //frequencies[] for all 256 values NOT including pseudo-eof
                for (int i = 0; i < ALPH_SIZE; i++) {
                    bos.writeBits(BITS_PER_INT, frequencies[i]);
                }
            } else if (headerFormat == STORE_TREE) {
                //([num leaf nodes * 9] + size of tree)
                bos.writeBits(BITS_PER_INT, calculateBitsInTree(hct.getRoot()));
                writeSTFHeaderWithPreorderTraversal(hct.getRoot(), bos);
            }
            addCompressedDataToBos(bis, bos); //add compressed data
            String peofEncoding = encodedMap.get(PSEUDO_EOF);
            addBitRepOfStringToBos(peofEncoding, bos); //add pseudo-eof

            bis.close();
            bos.close();
        }
        return compressedNumBits;
    }

    /*
    0 = non-leaf node
    1 = leaf node
    9 bits for value
    Pre-order = root, left, right
    */
    private void writeSTFHeaderWithPreorderTraversal(TreeNode currNode, BitOutputStream bos) {
        if (currNode.isLeaf()) {
            bos.writeBits(1, 1);
            //values 0-256 bc of pseudo-eof = 9 bits
            bos.writeBits(BITS_PER_WORD + 1, currNode.getValue());
        } else {
            bos.writeBits(1, 0);
            writeSTFHeaderWithPreorderTraversal(currNode.getLeft(), bos);
            writeSTFHeaderWithPreorderTraversal(currNode.getRight(), bos);
        }
    }

    private void addCompressedDataToBos(BitInputStream bis, BitOutputStream bos) throws IOException {
        int currByteValue = bis.readBits(BITS_PER_WORD);
        while (currByteValue != -1) {
            String encodedValueOfByte = encodedMap.get(currByteValue);
            //convert string encoding to bits and add to bos
            addBitRepOfStringToBos(encodedValueOfByte, bos);
            currByteValue = bis.readBits(BITS_PER_WORD);
        }
    }

    /*
    Converts String containing bit value into bits
     */
    private void addBitRepOfStringToBos(String s, BitOutputStream bos) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '0') {
                bos.writeBits(1, 0);
            } else { //has to be 1
                bos.writeBits(1, 1);
            }
        }
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     *
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream bis = new BitInputStream(in);
        BitOutputStream bos = new BitOutputStream(out);

        int magicNumber = bis.readBits(BITS_PER_INT);
        if (magicNumber != MAGIC_NUMBER) {
            myViewer.showError("file is not encoded with the huffman encoding algorithm");
            return -1;
        }

        headerFormat = bis.readBits(BITS_PER_INT);
        if (headerFormat == STORE_COUNTS) {
            covertHeaderToFrequencyArray(bis);
            this.hct = new HuffmanCodeTree(buildHuffmanPQ());
            hct.buildHuffmanTree();
        } else if (headerFormat == STORE_TREE) {
            bis.readBits(BITS_PER_INT); //ignore first 32 bits (size of tree)
            //this.hct = new HuffmanCodeTree(buildHuffmanPQ());
            hct.setRoot(hct.reconstructHuffmanTree(hct.getRoot(), bis));
        }

        return reconstructFile(bis, bos); //writes og text back to the file (decompresses actual data)
    }

    private void covertHeaderToFrequencyArray(BitInputStream bis) throws IOException {
        frequencies = new int[ALPH_SIZE + 1];
        for (int i = 0; i < ALPH_SIZE; i++) {
            int charFreq = bis.readBits(BITS_PER_INT);
            frequencies[i] = charFreq;
        }
    }

    //walk the tree to re-setup file
    private int reconstructFile(BitInputStream bis, BitOutputStream bos) throws IOException {
        int numUncompressedBits = 0;
        TreeNode currNode = hct.getRoot();
        boolean reachedPseudoEof = false;
        int currBit = bis.readBits(1);
        while (currBit != -1 && !reachedPseudoEof) {
            if (currBit == 0) { //go left
                currNode = currNode.getLeft();
            } else { //currBit == 1, go right
                currNode = currNode.getRight();
            }

            if (currNode.isLeaf()) { //only leaves have values + frequencies
                int currNodeValue = currNode.getValue();
                if (currNodeValue != PSEUDO_EOF) { //doesn't belong in og text
                    bos.writeBits(BITS_PER_WORD, currNodeValue);
                    numUncompressedBits += BITS_PER_WORD; // +8
                    currNode = hct.getRoot(); //reset tree traversal to root (new char)
                } else{
                    //need to end while loop once reached peof, can't rely on -1 because end of
                    // file might have extra 0s as padding
                    reachedPseudoEof = true;
                }
            }
            currBit = bis.readBits(1);
        }
        return numUncompressedBits;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
