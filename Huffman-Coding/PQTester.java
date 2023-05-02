public class PQTester {
    public static void main(String[] args){
        PriorityQueue pq = new PriorityQueue();
        pq.add(5);
        pq.add(2);
        pq.add(3);
        pq.add(1);
        pq.add(4);
        pq.add(3);
        System.out.println(pq);

        PriorityQueue pq2 = new PriorityQueue();
        pq2.add(new TreeNode(5, 2));
        pq2.add(new TreeNode(5, 1));
        pq2.add(new TreeNode(3, 4));
        pq2.add(new TreeNode(2, 5));
        pq2.add(new TreeNode(1, 6));
        pq2.add(new TreeNode(-1, 4));
        //expected: 5(1), 5(2), 3, -1, 2, 1
        System.out.println(pq2);
    }
}
